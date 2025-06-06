package org.grammarscope;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

class DrawableMatcher extends TypeSafeMatcher<View>
{
	private static final boolean COMPARE_MASKS = true;

	private static final int EMPTY = -1;

	private static final int ANY = -2;

	private final int expectedId;

	private String resourceName;

	DrawableMatcher(int expectedId)
	{
		super(View.class);
		this.expectedId = expectedId;
	}

	@Override
	protected boolean matchesSafely(View target)
	{
		if (!(target instanceof ImageView imageView))
		{
			return false;
		}
        if (expectedId == EMPTY)
		{
			return imageView.getDrawable() == null;
		}
		if (expectedId == ANY)
		{
			return imageView.getDrawable() != null;
		}
		Context context = target.getContext();
		Resources resources = context.getResources();
		Drawable expectedDrawable = AppCompatResources.getDrawable(context, expectedId);
		resourceName = resources.getResourceEntryName(expectedId);

		if (expectedDrawable == null)
		{
			return false;
		}

		Bitmap bitmap = getBitmap(imageView.getDrawable());
		Bitmap otherBitmap = getBitmap(expectedDrawable);
		if (!COMPARE_MASKS)
		{
			return bitmap.sameAs(otherBitmap);
		}
		Bitmap bitmap2 = toMask(bitmap);
		Bitmap otherBitmap2 = toMask(otherBitmap);
		return bitmap2.sameAs(otherBitmap2);
	}

	private Bitmap getBitmap(@NonNull Drawable drawable)
	{
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	@Override
	public void describeTo(@NonNull Description description)
	{
		description.appendText("with drawable from resource id: ");
		description.appendValue(expectedId);
		if (resourceName != null)
		{
			description.appendText("[");
			description.appendText(resourceName);
			description.appendText("]");
		}
	}

	private static Bitmap toMonochrome(@NonNull Bitmap bmp)
	{
		Bitmap bmpMonochrome = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpMonochrome);
		ColorMatrix ma = new ColorMatrix();
		ma.setSaturation(0);
		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(ma));
		canvas.drawBitmap(bmp, 0, 0, paint);
		return bmpMonochrome;
	}

	/**
	 * Mask based on transparency
	 *
	 * @param bmp bitmap
	 * @return mask bitmap
	 */
	private static Bitmap toMask(@NonNull Bitmap bmp)
	{
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bmpMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		int length = width * height;
		int[] inPixels = new int[length];
		int[] outPixels = new int[length];

		bmp.getPixels(inPixels, 0, width, 0, 0, width, height);

		int index = 0;
		for (int pix : inPixels)
		{
			int A = (pix >> 24) & 0xFF;
			boolean isWhite = A > 0;
			if (isWhite)
			{
				outPixels[index] = 0xFFFFFFFF;
			}
			else
			{
				outPixels[index] = 0x00000000; // transparent
			}
			index++;
		}
		bmpMask.setPixels(outPixels, 0, width, 0, 0, width, height);
		return bmpMask;
	}

	private static final float LUM_THRESHOLD = .2F;

	/**
	 * Mask based on luminosity
	 *
	 * @param bmp bitmap
	 * @return mask bitmap
	 */
	private static Bitmap toLumMask(@NonNull Bitmap bmp)
	{
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bmpMask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		int length = width * height;
		int[] inPixels = new int[length];
		int[] outPixels = new int[length];

		bmp.getPixels(inPixels, 0, width, 0, 0, width, height);

		final float factor = 255f;
		final float redBri = 0.2126f;
		final float greenBri = 0.2126f;
		final float blueBri = 0.0722f;

		int index = 0;
		for (int pix : inPixels)
		{
			int R = (pix >> 16) & 0xFF;
			int G = (pix >> 8) & 0xFF;
			int B = pix & 0xFF;

			float lum = (redBri * R / factor) + (greenBri * G / factor) + (blueBri * B / factor);
			boolean isWhite = lum > LUM_THRESHOLD;
			if (isWhite)
			{
				outPixels[index] = 0xFFFFFFFF; // white
			}
			else
			{
				outPixels[index] = 0xFF000000; // black
			}
			index++;
		}
		bmpMask.setPixels(outPixels, 0, width, 0, 0, width, height);
		return bmpMask;
	}
}

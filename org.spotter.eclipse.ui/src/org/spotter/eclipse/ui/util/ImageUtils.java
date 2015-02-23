/**
 * Copyright 2014 SAP AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spotter.eclipse.ui.util;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * An utility class to operate with images.
 * 
 * @author Denis Knoepfle
 * 
 */
public final class ImageUtils {

	private ImageUtils() {
	}

	/**
	 * Scales the given ImageData to fit in the client area.
	 * 
	 * @param clientArea
	 *            the area to fit in
	 * @param imageData
	 *            the image data to use
	 * @return the scaled image data fitting inside the client area
	 */
	public static ImageData clampToClientArea(Rectangle clientArea, ImageData imageData) {
		int width = imageData.width;
		int height = imageData.height;
		int maxImageWidth = clientArea.width;
		int maxImageHeight = clientArea.height;

		int scaledWidth = Math.min(width, maxImageWidth);
		int scaledHeight = Math.min(height, maxImageHeight);
		float wFactor = ((float) scaledWidth) / width;
		float hFactor = ((float) scaledHeight) / height;
		boolean scaleToFitWidth = wFactor <= hFactor;
		if (scaleToFitWidth) {
			width = scaledWidth;
			height *= wFactor;
		} else {
			width *= hFactor;
			height = scaledHeight;
		}

		return imageData.scaledTo(width, height);
	}

	/**
	 * Converts the image to <code>ImageData</code>. Expects the
	 * <code>DirectColorModel</code>.
	 * 
	 * @param bufferedImage
	 *            the image to convert
	 * @return the extracted image data
	 */
	public static ImageData convertToImageData(BufferedImage bufferedImage) {
		if (!(bufferedImage.getColorModel() instanceof DirectColorModel)) {
			throw new SWTException(SWT.ERROR_UNSUPPORTED_FORMAT);
		}

		DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
		PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
				colorModel.getBlueMask());
		ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(),
				palette);
		WritableRaster raster = bufferedImage.getRaster();
		int components = colorModel.getComponentSize().length;
		int[] pixels = new int[components];
		for (int x = 0; x < data.width; x++) {
			for (int y = 0; y < data.height; y++) {
				raster.getPixel(x, y, pixels);
				int pixel = palette.getPixel(new RGB(pixels[0], pixels[1], pixels[2]));
				data.setPixel(x, y, pixel);
			}
		}
		return data;
	}

}

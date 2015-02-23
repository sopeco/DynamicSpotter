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
package org.spotter.eclipse.ui.viewers;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.eclipse.ui.Activator;
import org.spotter.eclipse.ui.navigator.SpotterProjectParent;
import org.spotter.eclipse.ui.util.DialogUtils;

/**
 * A resource viewer which offers the possibility to view different types of
 * resource files such as images or pdfs.
 * <p>
 * The viewer looks best if placed within a composite with a
 * <code>FillLayout</code> or similar in order to use all the available space.
 * </p>
 * 
 * @author Denis Knoepfle
 * 
 */
public class ResourceViewer {

	private static final String NOT_AVAILABLE_IMG_TEXT = "not available";
	private static final String DLG_RESOURCE_TITLE = "Resource '%s'%s";

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceViewer.class);

	private String projectName;
	private String resourceFile;
	private Map<String, Shell> resourceShells;

	private Canvas canvas;
	private ImageData resourceImageData;
	private Image resourceImage;

	/**
	 * Creates a resource viewer under the given parent.
	 * 
	 * @param parent
	 *            the parent of this viewer
	 */
	public ResourceViewer(Composite parent) {
		this.projectName = null;
		this.resourceFile = null;
		this.resourceShells = new HashMap<>();
		this.canvas = new Canvas(parent, SWT.NONE);
		addCanvasListeners();
	}

	/**
	 * Clears the viewer and disposes of all images.
	 */
	public void clear() {
		projectName = null;
		resourceFile = null;
		if (resourceImage != null) {
			resourceImage.dispose();
			resourceImage = null;
		}
		resourceImageData = null;
		canvas.setBackgroundImage(null);
	}

	/**
	 * Disposes of this viewer. This method should be called by clients to clean
	 * up.
	 */
	public void dispose() {
		Set<Shell> shells = new HashSet<>();
		shells.addAll(resourceShells.values());
		for (Shell shell : shells) {
			shell.close();
		}

		if (resourceImage != null) {
			resourceImage.dispose();
			resourceImage = null;
		}
	}

	/**
	 * Sets the resource of the viewer.
	 * 
	 * @param resourceFile
	 *            the absolute filename of the file to display
	 * @param projectName
	 *            the name of the project the resource belongs to or
	 *            <code>null</code>
	 */
	public void setResource(String resourceFile, String projectName) {
		if (resourceImage != null) {
			resourceImage.dispose();
			resourceImage = null;
		}
		resourceImageData = null;
		this.resourceFile = resourceFile;
		this.projectName = projectName;
		File file = new File(resourceFile);
		int canvasWidth = canvas.getBounds().width;
		int canvasHeight = canvas.getBounds().height;
		boolean isCanvasVisible = canvasWidth > 0 && canvasHeight > 0;

		if (isCanvasVisible) {
			boolean loadSuccessful = false;
			if (file.exists()) {
				try {
					resourceImageData = new ImageData(resourceFile);
					int width = Math.min(canvasWidth, resourceImageData.width);
					int height = Math.min(canvasHeight, resourceImageData.height);
					resourceImage = new Image(canvas.getDisplay(), resourceImageData.scaledTo(width, height));
					loadSuccessful = true;
				} catch (SWTException e) {
					String message = "Could not load the resource!";
					String cause;
					switch (e.code) {
					case SWT.ERROR_IO:
						cause = "I/O exception occured";
						break;
					case SWT.ERROR_INVALID_IMAGE:
						cause = "Image file contains invalid data";
						break;
					case SWT.ERROR_UNSUPPORTED_FORMAT:
						cause = "Image file contains an unsupported or unrecognized format";
						break;
					default:
						cause = "unknown";
						break;
					}
					DialogUtils.openError(DialogUtils.appendCause(message, cause, true));
					LOGGER.error(DialogUtils.appendCause(message, cause, false), e);
				}
			}
			if (!loadSuccessful) {
				// draw "not available image" picture using GC
				final Display display = canvas.getDisplay();
				final Rectangle bounds = canvas.getBounds();
				resourceImage = new Image(display, bounds);
				drawNotAvailableImage(resourceImage, display, bounds);
			}
		}

		canvas.setBackgroundImage(resourceImage);
	}

	private void addCanvasListeners() {
		canvas.addMouseTrackListener(new MouseTrackAdapter() {

			@Override
			public void mouseEnter(MouseEvent e) {
				if (resourceImage != null) {
					Cursor zoomCursor = Display.getDefault().getSystemCursor(SWT.CURSOR_HAND);
					canvas.setCursor(zoomCursor);
				}
			}

			@Override
			public void mouseExit(MouseEvent e) {
				canvas.setCursor(null);
			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (resourceFile != null && resourceImageData != null) {
					if (resourceShells.containsKey(resourceFile)) {
						resourceShells.get(resourceFile).setFocus();
					} else {
						openResourcePopupShell(e.display, resourceFile);
					}
				}
			}
		});

		canvas.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				resizeCanvasImage();
			}
		});
	}

	private void resizeCanvasImage() {
		if (resourceImageData == null) {
			return;
		} else if (resourceImage != null) {
			resourceImage.dispose();
		}
		int canvasWidth = canvas.getBounds().width;
		int canvasHeight = canvas.getBounds().height;
		if (canvasWidth > 0 && canvasHeight > 0) {
			int width = Math.min(canvasWidth, resourceImageData.width);
			int height = Math.min(canvasHeight, resourceImageData.height);
			resourceImage = new Image(canvas.getDisplay(), resourceImageData.scaledTo(width, height));
			canvas.setBackgroundImage(resourceImage);
		}
	}

	private void drawNotAvailableImage(Image image, Display display, Rectangle bounds) {
		GC gc = new GC(image);
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.setForeground(display.getSystemColor(SWT.COLOR_RED));

		gc.fillRectangle(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, 0, bounds.width, bounds.height);
		gc.drawLine(0, bounds.height, bounds.width, 0);

		final int flags = SWT.DRAW_TRANSPARENT;
		Point textExtent = gc.textExtent(NOT_AVAILABLE_IMG_TEXT, flags);

		int x = (bounds.width - textExtent.x) / 2;
		int y = bounds.height - textExtent.y;
		gc.drawText(NOT_AVAILABLE_IMG_TEXT, x, y, flags);
		gc.dispose();
	}

	private void openResourcePopupShell(final Display display, final String resourceFile) {
		final Shell popupShell = new Shell(SWT.BORDER | SWT.RESIZE | SWT.CLOSE);
		Label label = new Label(popupShell, SWT.NONE);
		Rectangle clientArea = display.getPrimaryMonitor().getClientArea();

		final Image image = new Image(display, createScaledImageData(clientArea));
		label.setImage(image);

		popupShell.setLayout(new FillLayout());
		popupShell.setImage(Activator.getImage(SpotterProjectParent.IMAGE_PATH));
		String projectSuffix = projectName != null ? " (" + projectName + ")" : "";

		Path resourcePath = new File(resourceFile).toPath().getFileName();
		String resourceName = resourcePath != null ? resourcePath.toString() : "-";
		popupShell.setText(String.format(DLG_RESOURCE_TITLE, resourceName, projectSuffix));
		popupShell.pack();

		Rectangle shellRect = popupShell.getBounds();
		int x = clientArea.x + (clientArea.width - shellRect.width) / 2;
		int y = clientArea.y + (clientArea.height - shellRect.height) / 2;
		popupShell.setLocation(x, y);

		popupShell.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					popupShell.close();
				}
			}
		});
		popupShell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				image.dispose();
				resourceShells.remove(resourceFile);
			}
		});

		resourceShells.put(resourceFile, popupShell);
		popupShell.open();
	}

	private ImageData createScaledImageData(Rectangle clientArea) {
		int width = resourceImageData.width;
		int height = resourceImageData.height;
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

		return resourceImageData.scaledTo(width, height);
	}

}

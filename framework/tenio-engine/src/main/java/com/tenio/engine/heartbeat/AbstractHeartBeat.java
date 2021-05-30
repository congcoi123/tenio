/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.engine.heartbeat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.tenio.common.loggers.AbstractLogger;
import com.tenio.common.utilities.MathUtility;
import com.tenio.common.utilities.TimeUtility;
import com.tenio.engine.message.EMessage;
import com.tenio.engine.physic2d.graphic.Paint;

/**
 * The game loop is the overall flow control for the entire game program. It's a
 * loop because the game keeps doing a series of actions over and over again
 * until the user quits. If a game runs at 60 FPS (frames per second), this
 * means that the game loop completes 60 iterations every second.
 */
public abstract class AbstractHeartBeat extends AbstractLogger implements Callable<Void>, ActionListener {

	/**
	 * The target frame per second
	 */
	private static final int TARGET_FPS = 60;
	/**
	 * Calculate how many ns each frame should take for our target game hertz.
	 */
	private static final double TIME_BETWEEN_UPDATES = 1000000000 / TARGET_FPS;
	private static final double TIME_BETWEEN_RENDER = 1000000000 / TARGET_FPS;
	/**
	 * At the very most we will update the game this many times before a new render.
	 * If you're worried about visual hitches more than perfect timing, set this to
	 * 1.
	 */
	private static final int MAX_UPDATES_BEFORE_RENDER = 5;

	/**
	 * A Set is used as the container for the delayed messages because of the
	 * benefit of automatic sorting and avoidance of duplicates. Messages are sorted
	 * by their dispatch time. See {@link HMessage}
	 */
	private TreeSet<HMessage> __listener;

	/**
	 * For displaying debugger
	 */
	private JFrame __frame;
	/**
	 * For holding a frame
	 */
	private GamePanel __panel;

	/**
	 * Can be customized action button
	 */
	private JButton __action1;
	/**
	 * Can be customized action button
	 */
	private JButton __action2;
	/**
	 * Can be customized action button
	 */
	private JButton __action3;

	/**
	 * The screen view width (in pixel)
	 */
	private int __viewWidth;
	/**
	 * The screen view height (in pixel)
	 */
	private int __viewHeight;

	/**
	 * Frame per second
	 */
	private int __currentFps;
	private int __frameCount;

	private int __currentCCU;

	private boolean __running;
	private boolean __debugging;

	/**
	 * Create a new instance with default FPS value, see {@value #TARGET_FPS}
	 */
	public AbstractHeartBeat() {
		__currentFps = TARGET_FPS;
		__frameCount = 0;
		__currentCCU = 0;

		__running = true;
		__debugging = false;
	}

	/**
	 * Create a new instance
	 * 
	 * @param viewWidth  the view width in pixel
	 * @param viewHeight the view height in pixel
	 */
	public AbstractHeartBeat(int viewWidth, int viewHeight) {
		this();
		__viewWidth = viewWidth;
		__viewHeight = viewHeight;
	}

	/**
	 * Set the listener for the heart-beat. It's used to communicate with outside.
	 * This method must be called before {@link #call()}
	 * 
	 * @param listener the messages' listener
	 */
	public void setMessageListener(TreeSet<HMessage> listener) {
		__listener = listener;
	}

	/**
	 * Start a new life cycle (game loop)
	 */
	private void __start() {
		// seed random number generator
		MathUtility.setSeed(0);
		__onCreate();
		// main loop
		__loop();
	}

	/**
	 * Pause game loop
	 * 
	 * @param pause set <b>true</b> if you want to pause the current game loop
	 */
	protected void __pause(final boolean pause) {
		if (pause) { // pause
			__running = !pause;
			__onPause();
		} else { // resume
			__onResume();
			__running = !pause;
		}
	}

	/**
	 * Retrieves the FPS
	 * 
	 * @return the FPS
	 */
	protected int __getFPS() {
		return __currentFps;
	}

	/**
	 * Display a window for debugging
	 * 
	 * @param title the title of debug window
	 */
	public void debug(String title) {
		__frame = new JFrame();
		__frame.setTitle(title);

		__panel = new GamePanel();

		__action1 = new JButton("Action 1");
		__action2 = new JButton("Action 2");
		__action3 = new JButton("Action 3");

		var cp = __frame.getContentPane();
		cp.setLayout(new BorderLayout());

		var p = new JPanel();
		p.setLayout(new GridLayout(1, 2));
		p.add(__action1);
		p.add(__action2);
		p.add(__action3);

		cp.add(__panel, BorderLayout.CENTER);
		cp.add(p, BorderLayout.SOUTH);

		__frame.setSize(__viewWidth, __viewHeight);

		__action1.addActionListener(this);
		__action2.addActionListener(this);
		__action3.addActionListener(this);

		__frame.setVisible(true);
		__debugging = true;
	}

	public void setCCU(int currentCCU) {
		__currentCCU = currentCCU;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		var s = e.getSource();
		if (s == __action1) {
			__onAction1();
		} else if (s == __action2) {
			__onAction2();
		} else if (s == __action3) {
			__onAction3();
		}
	}

	/**
	 * The main loop, the
	 * <a href="http://www.java-gaming.org/index.php?topic=24220.0">reference</a>
	 */
	private void __loop() {
		// We will need the last update time.
		double lastUpdateTime = System.nanoTime();
		// Store the last time we rendered.
		double lastRenderTime = System.nanoTime();

		// Simple way of finding FPS.
		int lastSecondTime = (int) (lastUpdateTime / 1000000000);

		while (__running) {
			double now = System.nanoTime();
			int updateCount = 0;

			// Do as many game updates as we need to, potentially playing catch-up.
			while (now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER) {
				float delta = 1.0f / __currentFps;

				// Message communication
				// get current time
				double currentTime = TimeUtility.currentTimeSeconds();

				// now peek at the queue to see if any telegrams need dispatching.
				// remove all telegrams from the front of the queue that have gone
				// past their sell by date
				while (!__listener.isEmpty() && (__listener.last().getDelayTime() < currentTime)) {
					// read the message from the front of the queue
					var message = __listener.last();
					// listening
					__onMessage(message.getMessage());
					// remove it from the queue
					__listener.remove(message);
				}

				// Main update
				__onUpdate(delta);

				lastUpdateTime += TIME_BETWEEN_UPDATES;
				updateCount++;
			}

			// If for some reason an update takes forever, we don't want to do an insane
			// number of catch-ups.
			// If you were doing some sort of game that needed to keep EXACT time, you would
			// get rid of this.
			if (now - lastUpdateTime > TIME_BETWEEN_UPDATES) {
				lastUpdateTime = now - TIME_BETWEEN_UPDATES;
			}

			// Render. To do so, we need to calculate interpolation for a smooth render.
			// float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) /
			// TIME_BETWEEN_UPDATES));
			if (__debugging) {
				__draw();
			}
			lastRenderTime = now;

			// Update the frames we got.
			int thisSecond = (int) (lastUpdateTime / 1000000000);
			if (thisSecond > lastSecondTime) {
				__currentFps = __frameCount;
				__frameCount = 0;
				lastSecondTime = thisSecond;
			}

			// Yield until it has been at least the target time between renders. This saves
			// the CPU from hogging.
			while (now - lastRenderTime < TIME_BETWEEN_RENDER && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
				Thread.yield();

				// This stops the application from consuming all your CPU. It makes this
				// slightly less
				// accurate, but is worth it.
				// You can remove this line and it will still work (better), your CPU just
				// climbs on certain OSes.
				// FYI on some OS's this can cause pretty bad shuttering. Scroll down and have a
				// look at different peoples' solutions to this.
				try {
					Thread.sleep(1);
				} catch (Exception e) {
					// can dispose
					__onDispose();
					return;
				}

				now = System.nanoTime();
			}

			// update counter
			__frameCount++;
		}
	}

	/**
	 * Repaint or refresh the screen
	 */
	private void __draw() {
		__panel.repaint();
	}

	/**
	 * The debug window
	 */
	private class GamePanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -768827954326690612L;

		private Paint __paint = Paint.getInstance();

		@Override
		public void paintComponent(Graphics graphic) {
			__paint.startDrawing(graphic);
			// fill our back buffer with white
			__paint.fillRect(Color.WHITE, 0, 0, __viewWidth, __viewHeight);
			__onRender(__paint);

			// show FPS
			graphic.setColor(Color.BLACK);
			graphic.drawString(String.format("FPS: %d", __currentFps), 5, 10);

			// show CCU
			graphic.setColor(Color.RED);
			graphic.drawString(String.format("CCU: %d", __currentCCU), __viewWidth / 2 - 30, 10);
		}

	}

	public void setTextAction1(String text, Color color) {
		__action1.setText(text);
		__action1.setBackground(color);
	}

	public void setTextAction2(String text, Color color) {
		__action2.setText(text);
		__action2.setBackground(color);
	}

	public void setTextAction3(String text, Color color) {
		__action3.setText(text);
		__action3.setBackground(color);
	}

	@Override
	public Void call() throws Exception {
		__start();
		return null;
	}

	/**
	 * It is called when start a game loop
	 */
	protected abstract void __onCreate();

	/**
	 * It is called when the heart-beat receives a message from outside
	 * 
	 * @param message the coming message, see {@link EMessage}
	 */
	protected abstract void __onMessage(EMessage message);

	/**
	 * It is called every frame in a game loop
	 * 
	 * @param deltaTime the time between two frames
	 */
	protected abstract void __onUpdate(float deltaTime);

	/**
	 * It is called every frame after {@link #__onUpdate(float)}
	 * 
	 * @param paint the painting object
	 */
	protected abstract void __onRender(Paint paint);

	/**
	 * It is called when you call {@link #__pause(boolean)} with <b>true</b>
	 * parameter
	 */
	protected abstract void __onPause();

	/**
	 * It is called when you call {@link #__pause(boolean)} with <b>false</b>
	 * parameter
	 */
	protected abstract void __onResume();

	/**
	 * It is called when the game loop is stopped or destroyed
	 */
	protected abstract void __onDispose();

	/**
	 * Customize the button 1 action
	 */
	protected abstract void __onAction1();

	/**
	 * Customize the button 2 action
	 */
	protected abstract void __onAction2();

	/**
	 * Customize the button 3 action
	 */
	protected abstract void __onAction3();

}

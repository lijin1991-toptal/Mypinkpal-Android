package com.brodev.socialapp.android.manager;

public class UIHelper {
    public static boolean homeKeyPressed;
    private static boolean justLaunched = true;

    /**
     * Check if the app was just launched. If the app was just launched then
     * assume that the HOME key will be pressed next unless a navigation event
     * by the user or the app occurs. Otherwise the user or the app navigated to
     * the activity so the HOME key was not pressed.
     */
    public static void checkJustLaunced() {
        if (justLaunched) {
            homeKeyPressed = true;
            justLaunched = false;
        } else {
            homeKeyPressed = false;
        }
    }

    /**
     * Check if the HOME key was pressed. If the HOME key was pressed then the
     * app will be killed either safely or quickly. Otherwise the user or the
     * app is navigating away from the activity so assume that the HOME key will
     * be pressed next unless a navigation event by the user or the app occurs.
     *
     * @param killSafely
     *            Primitive boolean which indicates whether the app should be
     *            killed safely or quickly when the HOME key is pressed.
     *
     * @see {@link UIHelper.killApp}
     */
    public static void checkHomeKeyPressed(boolean killSafely) {
        if (homeKeyPressed) {
            killApp(true);
        } else {
            homeKeyPressed = true;
        }
    }

    /**
     * Kill the app either safely or quickly. The app is killed safely by
     * killing the virtual machine that the app runs in after finalizing all
     * {@link Object}s created by the app. The app is killed quickly by abruptly
     * killing the process that the virtual machine that runs the app runs in
     * without finalizing all {@link Object}s created by the app. Whether the
     * app is killed safely or quickly the app will be completely created as a
     * new app in a new virtual machine running in a new process if the user
     * starts the app again.
     *
     * <P>
     * <B>NOTE:</B> The app will not be killed until all of its threads have
     * closed if it is killed safely.
     * </P>
     *
     * <P>
     * <B>NOTE:</B> All threads running under the process will be abruptly
     * killed when the app is killed quickly. This can lead to various issues
     * related to threading. For example, if one of those threads was making
     * multiple related changes to the database, then it may have committed some
     * of those changes but not all of those changes when it was abruptly
     * killed.
     * </P>
     *
     * @param killSafely
     *            Primitive boolean which indicates whether the app should be
     *            killed safely or quickly. If true then the app will be killed
     *            safely. Otherwise it will be killed quickly.
     */
    @SuppressWarnings("deprecation")
    public static void killApp(boolean killSafely) {
        if (killSafely) {
			/*
			 * Notify the system to finalize and collect all objects of the app
			 * on exit so that the virtual machine running the app can be killed
			 * by the system without causing issues. NOTE: If this is set to
			 * true then the virtual machine will not be killed until all of its
			 * threads have closed.
			 */
            System.runFinalizersOnExit(true);

			/*
			 * Force the system to close the app down completely instead of
			 * retaining it in the background. The virtual machine that runs the
			 * app will be killed. The app will be completely created as a new
			 * app in a new virtual machine running in a new process if the user
			 * starts the app again.
			 */
            System.exit(0);
        } else {
			/*
			 * Alternatively the process that runs the virtual machine could be
			 * abruptly killed. This is the quickest way to remove the app from
			 * the device but it could cause problems since resources will not
			 * be finalized first. For example, all threads running under the
			 * process will be abruptly killed when the process is abruptly
			 * killed. If one of those threads was making multiple related
			 * changes to the database, then it may have committed some of those
			 * changes but not all of those changes when it was abruptly killed.
			 */
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }
}


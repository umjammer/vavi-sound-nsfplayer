/**
 * <p>The rendering part of FamiTracker is mainly responsible for running FTM files
 * and trying to output audio sample data.
 * This package depends on the zdream.nsfplayer.sound package for audio rendering.
 * Starting from version v0.3.0, most of the executable components are moved to
 * the package zdream.nsfplayer.ftm.executor.
 *
 * <p>If you need to render and play FTM files, it is recommended to use
 * {@link zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer} class.
 * <p>If you need to execute the FTM file, it is recommended to use the
 * {@link zdream.nsfplayer.ftm.executor.FamiTrackerExecutor} class.
 * </p>
 *
 * @author Zdream
 * @since v0.2.1
 */

package zdream.nsfplayer.ftm.renderer;
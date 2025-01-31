/**
 * <p>A package of classes related to vocal logic.
 * <p>This package contains a number of subclasses of {@link zdream.nsfplayer.sound.AbstractNsfSound},
 * which act as endpoints of the implementation artifacts and are used to receive kernel data
 * from the core of the implementation artifacts and send audio data to the rendering artifacts.
 * <p>In version v0.2.x, the mixer, which is the main body of the rendering artifact,
 * is placed in this package.
 * Starting with v0.3.0, these classes have been moved to the {@link zdream.nsfplayer.mixer} package,
 * thus officially separating the execution and rendering components.
 * </p>
 *
 * @author Zdream
 * @since v0.1
 */

package zdream.nsfplayer.sound;
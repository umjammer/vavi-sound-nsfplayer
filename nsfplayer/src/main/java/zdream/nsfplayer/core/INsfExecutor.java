package zdream.nsfplayer.core;

/**
 * <p>NSF Implementation Interface
 * </p>
 *
 * @param <T> Depending on the demand for export equipment,
 *            the number of imported products depends on the initial production.
 *            In the native Renderer system, this is for each Audio packaging category.
 * @author Zdream
 * @since v0.3.0
 */
public interface INsfExecutor<T> extends IEnable, IResetable {

    /**
     * In the native Renderer system, this is for each Audio packaging category.
     *
     * @param t Import Data
     */
    void ready(T t);

    /**
     * The duration of the exercise is different depending on the length of time.
     */
    void tick();

}

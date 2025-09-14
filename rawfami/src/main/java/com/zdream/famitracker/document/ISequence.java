package com.zdream.famitracker.document;

/**
 * <p>This interface is used to provide methods for custom data output.
 * <p>This class is a pure virtual interface to Sequence, which can be used by custom exporters
 *
 * @author Zdream
 */
public interface ISequence {

    int getItem(int index);

    int getItemCount();

    int getLoopPoint();

}

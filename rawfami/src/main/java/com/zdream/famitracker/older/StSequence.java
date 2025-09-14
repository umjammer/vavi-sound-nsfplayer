package com.zdream.famitracker.older;

import com.zdream.famitracker.FamitrackerTypes;


/**
 * <p>Old version of Sequence
 * <p>Old sequence list, kept for compability
 *
 * @author Zdream
 */
public class StSequence {

    public int count;
    public final byte[] length = new byte[FamitrackerTypes.MAX_SEQUENCE_ITEMS];
    public final byte[] value = new byte[FamitrackerTypes.MAX_SEQUENCE_ITEMS];
}

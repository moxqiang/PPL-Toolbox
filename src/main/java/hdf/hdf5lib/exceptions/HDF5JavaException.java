/*****************************************************************************
 * Copyright by The HDF Group.                                               *
 * Copyright by the Board of Trustees of the University of Illinois.         *
 * All rights reserved.                                                      *
 *                                                                           *
 * This file is part of the HDF Java Products distribution.                  *
 * The full copyright notice, including terms governing use, modification,   *
 * and redistribution, is contained in the file, COPYING.                    *
 * COPYING can be found at the root of the source code distribution tree.    *
 * If you do not have access to this file, you may request a copy from       *
 * help@hdfgroup.org.                                                        *
 ****************************************************************************/

package hdf.hdf5lib.exceptions;

/**
 * <p>
 * The class HDF5JavaException returns errors from the Java wrapper of theHDF5
 * library.
 * <p>
 * These errors include Java configuration errors, security violations, and
 * resource exhaustion.
 */
public class HDF5JavaException extends HDF5Exception {
    /**
     * Constructs an <code>HDF5JavaException</code> with no specified detail
     * message.
     */
    public HDF5JavaException() {
        super();
    }

    /**
     * Constructs an <code>HDF5JavaException</code> with the specified detail
     * message.
     *
     * @param s
     *            the detail message.
     */
    public HDF5JavaException(String s) {
        super(s);
    }
}

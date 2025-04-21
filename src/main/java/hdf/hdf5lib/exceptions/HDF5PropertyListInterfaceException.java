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
 * The class HDF5LibraryException returns errors raised by the HDF5 library.
 * <p>
 * This sub-class represents HDF-5 major error code <b>H5E_PLIST</b>
 */

public class HDF5PropertyListInterfaceException extends HDF5LibraryException {
    /**
     * Constructs an <code>HDF5PropertyListInterfaceException</code> with no
     * specified detail message.
     */
    public HDF5PropertyListInterfaceException() {
        super();
    }

    /**
     * Constructs an <code>HDF5PropertyListInterfaceException</code> with the
     * specified detail message.
     *
     * @param s
     *            the detail message.
     */
    public HDF5PropertyListInterfaceException(String s) {
        super(s);
    }
}

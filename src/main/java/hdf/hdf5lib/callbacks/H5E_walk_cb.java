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

package hdf.hdf5lib.callbacks;

import hdf.hdf5lib.structs.H5E_error2_t;

//Information class for link callback(for H5Ewalk)
public interface H5E_walk_cb extends Callbacks {
    int callback(int nidx, H5E_error2_t info, H5E_walk_t op_data);
}

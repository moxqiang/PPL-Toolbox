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

import hdf.hdf5lib.structs.H5L_info_t;

//Information class for link callback(for H5Lvisit/H5Lvisit_by_name)
public interface H5L_iterate_cb extends Callbacks {
    int callback(int group, String name, H5L_info_t info, H5L_iterate_t op_data);
}

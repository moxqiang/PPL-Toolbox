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

//Information class for link callback(for H5Pcreate_class)
public interface H5P_cls_copy_func_cb extends Callbacks {
    int callback(int new_prop_id, int old_prop_id, H5P_cls_copy_func_t copy_data);
}

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

package hdf.hdf5lib.structs;

import java.io.Serializable;

//Information struct for group (for H5Gget_info/H5Gget_info_by_name/H5Gget_info_by_idx)
public class H5_ih_info_t implements Serializable {
    private static final long serialVersionUID = -142238015615462707L;
    public long     index_size;     /* btree and/or list */
    public long     heap_size;

    H5_ih_info_t (long index_size, long heap_size)
    {
        this.index_size = index_size;
        this.heap_size = heap_size;
    }
}

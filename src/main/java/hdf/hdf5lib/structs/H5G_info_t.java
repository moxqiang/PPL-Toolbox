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
public class H5G_info_t implements Serializable{
    private static final long serialVersionUID = -3746463015312132912L;
    public int storage_type; // Type of storage for links in group
    public long nlinks; // Number of links in group
    public long max_corder; // Current max. creation order value for group
    public boolean mounted; // Whether group has a file mounted on it
}

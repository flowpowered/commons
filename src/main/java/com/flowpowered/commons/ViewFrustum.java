/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commons;

import com.flowpowered.math.matrix.Matrix3f;
import com.flowpowered.math.matrix.Matrix4f;
import com.flowpowered.math.vector.Vector3f;

/**
 * A view frustum defined by the camera view and projection. Used to check for intersection with object to determine whether or not they're visible.
 */
public class ViewFrustum {
    private final float[][] frustum = new float[6][4];
    private Vector3f[] vertices;
    private Vector3f position;

    /**
     * Updates the frustum to match the view and projection matrix.
     *
     * @param projection The projection matrix
     * @param view The view matrix
     */
    public void update(Matrix4f projection, Matrix4f view) {
        // http://www.crownandcutlass.com/features/technicaldetails/frustum.html
        final float[] clip = projection.mul(view).toArray(true);
        // Extract the numbers for the RIGHT plane
        frustum[0][0] = clip[3] - clip[0];
        frustum[0][1] = clip[7] - clip[4];
        frustum[0][2] = clip[11] - clip[8];
        frustum[0][3] = clip[15] - clip[12];
        // Extract the numbers for the LEFT plane
        frustum[1][0] = clip[3] + clip[0];
        frustum[1][1] = clip[7] + clip[4];
        frustum[1][2] = clip[11] + clip[8];
        frustum[1][3] = clip[15] + clip[12];
        // Extract the BOTTOM plane
        frustum[2][0] = clip[3] + clip[1];
        frustum[2][1] = clip[7] + clip[5];
        frustum[2][2] = clip[11] + clip[9];
        frustum[2][3] = clip[15] + clip[13];
        // Extract the TOP plane
        frustum[3][0] = clip[3] - clip[1];
        frustum[3][1] = clip[7] - clip[5];
        frustum[3][2] = clip[11] - clip[9];
        frustum[3][3] = clip[15] - clip[13];
        // Extract the FAR plane
        frustum[4][0] = clip[3] - clip[2];
        frustum[4][1] = clip[7] - clip[6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];
        // Extract the NEAR plane
        frustum[5][0] = clip[3] + clip[2];
        frustum[5][1] = clip[7] + clip[6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];
        // Invalidate the caches
        vertices = null;
        position = null;
    }

    /**
     * Returns a new array containing the vertices of the frustum. They are in the following order, given by the intersection of the planes:
     * <p/>
     * RIGHT - TOP - NEAR
     * <p/>
     * RIGHT - TOP - FAR
     * <p/>
     * RIGHT - BOTTOM - NEAR
     * <p/>
     * RIGHT - BOTTOM - FAR
     * <p/>
     * LEFT - TOP - NEAR
     * <p/>
     * LEFT - TOP - FAR
     * <p/>
     * LEFT - BOTTOM - NEAR
     * <p/>
     * LEFT - BOTTOM - FAR
     *
     * @return An array containing the frustum vertices
     */
    public Vector3f[] getVertices() {
        if (vertices == null) {
            // Recalculate the points if the cache is invalid
            computePoints();
        }
        return vertices.clone();
    }

    /**
     * Returns the position of the frustum.
     *
     * @return The frustum position
     */
    public Vector3f getPosition() {
        if (position == null) {
            // Recalculate the point if the cache is invalid
            computePoints();
        }
        return position;
    }

    private void computePoints() {
        vertices = new Vector3f[8];
        // RIGHT - TOP - NEAR
        vertices[0] = getIntersection(0, 3, 5);
        // RIGHT - TOP - FAR
        vertices[1] = getIntersection(0, 3, 4);
        // RIGHT - BOTTOM - NEAR
        vertices[2] = getIntersection(0, 2, 5);
        // RIGHT - BOTTOM - FAR
        vertices[3] = getIntersection(0, 2, 4);
        // LEFT - TOP - NEAR
        vertices[4] = getIntersection(1, 3, 5);
        // LEFT - TOP - FAR
        vertices[5] = getIntersection(1, 3, 4);
        // LEFT - BOTTOM - NEAR
        vertices[6] = getIntersection(1, 2, 5);
        // LEFT - BOTTOM - FAR
        vertices[7] = getIntersection(1, 2, 4);
        position = vertices[0].add(vertices[2]).add(vertices[4]).add(vertices[6]).div(4);
    }

    private Vector3f getIntersection(int p1, int p2, int p3) {
         /*
         Intersection of 3 planes

         p1: ax + by + cz + d = 0
         p2: ex + fy + gz + h = 0
         p3: ix + jy + kz + l = 0

         |a b c||x|   |-d|
         |e f g||y| = |-h|
         |i j k||z|   |-l|
            A *  r  =   b

         r = (A^-1) * b

         r is our point of intersection
        */
        final Matrix3f a = new Matrix3f(
                frustum[p1][0], frustum[p1][1], frustum[p1][2],
                frustum[p2][0], frustum[p2][1], frustum[p2][2],
                frustum[p3][0], frustum[p3][1], frustum[p3][2]
        );
        final Vector3f b = new Vector3f(
                -frustum[p1][3], -frustum[p2][3], -frustum[p3][3]
        );
        final Matrix3f aInv = a.invert();
        if (aInv == null) {
            return null;
        }
        return aInv.transform(b);
    }

    /**
     * Compute the distance between a point and the given plane.
     *
     * @param i The id of the plane
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @param z The z coordinate of the point
     * @return The distance
     */
    private float distance(int i, float x, float y, float z) {
        return frustum[i][0] * x + frustum[i][1] * y + frustum[i][2] * z + frustum[i][3];
    }

    /**
     * Checks if the frustum of this camera intersects the given cuboid vertices.
     *
     * @param vertices The cuboid local vertices to check the frustum against
     * @param position The position of the cuboid
     * @return Whether or not the frustum intersects the cuboid
     */
    public boolean intersectsCuboid(Vector3f[] vertices, Vector3f position) {
        return intersectsCuboid(vertices, position.getX(), position.getY(), position.getZ());
    }

    /**
     * Checks if the frustum of this camera intersects the given cuboid vertices.
     *
     * @param vertices The cuboid local vertices to check the frustum against
     * @param x The x coordinate of the cuboid position
     * @param y The y coordinate of the cuboid position
     * @param z The z coordinate of the cuboid position
     * @return Whether or not the frustum intersects the cuboid
     */
    public boolean intersectsCuboid(Vector3f[] vertices, float x, float y, float z) {
        if (vertices.length != 8) {
            throw new IllegalArgumentException("A cuboid has 8 vertices, not " + vertices.length);
        }
        planes:
        for (int i = 0; i < 6; i++) {
            for (Vector3f vertex : vertices) {
                if (distance(i, vertex.getX() + x, vertex.getY() + y, vertex.getZ() + z) > 0) {
                    continue planes;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Checks if the frustum contains the given point.
     *
     * @param point The point to check if the frustum contains
     * @return True if the frustum contains the point
     */
    public boolean contains(Vector3f point) {
        return contains(point.getX(), point.getY(), point.getZ());
    }

    /**
     * Checks if the frustum contains the given point.
     *
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point
     * @param z The z coordinate of the point
     * @return True if the frustum contains the point
     */
    public boolean contains(float x, float y, float z) {
        for (int i = 0; i < 6; i++) {
            if (distance(i, x, y, z) <= 0) {
                return false;
            }
        }
        return true;
    }
}

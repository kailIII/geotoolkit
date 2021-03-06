/*
 *    Geotoolkit.org - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2001-2012, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2009-2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.referencing.operation.transform;

import java.util.Arrays;

import org.opengis.util.FactoryException;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;

import org.geotoolkit.factory.Hints;
import org.geotoolkit.factory.FactoryFinder;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.geotoolkit.referencing.operation.matrix.GeneralMatrix;
import org.geotoolkit.internal.referencing.SeparableTransform;
import org.geotoolkit.resources.Errors;

import org.apache.sis.referencing.operation.transform.LinearTransform;
import org.apache.sis.referencing.operation.transform.PassThroughTransform;
import org.apache.sis.referencing.operation.transform.Accessor;
import org.apache.sis.util.ArraysExt;


/**
 * A utility class for the separation of {@linkplain ConcatenatedTransform concatenation} of
 * {@linkplain PassThroughTransform pass through transforms}. Given an arbitrary
 * {@linkplain MathTransform math transform}, this utility class will returns a new math transform
 * that operates only of a given set of source dimensions. For example if the supplied
 * {@code transform} has (<var>x</var>, <var>y</var>, <var>z</var>) inputs and
 * (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs, then
 * the following code:
 *
 * {@preformat java
 *     addSourceDimensionRange(0, 2);
 *     MathTransform mt = separate(transform);
 * }
 *
 * will returns a transform with (<var>x</var>, <var>y</var>) inputs and (probably)
 * (<var>longitude</var>, <var>latitude</var>) outputs. The later can be verified with
 * a call to {@link #getTargetDimensions}.
 *
 * @author Martin Desruisseaux (IRD)
 * @author Simone Giannecchini (Geosolutions)
 * @version 3.00
 *
 * @todo This class contains a set of static methods that could be factored out in
 *       some kind of {@code org.geotoolkit.util.SortedIntegerSet} implementation.
 *
 * @todo Consider providing a {@code subTransform(DimensionFilter)} method in
 *       {@link AbstractMathTransform}, and move some {@code DimensionFilter}
 *       code in {@code AbstractMathTransform} sub-classes. This would allow us
 *       to separate transforms that are defined in downstream modules, like NetCDF.
 *
 * @since 2.1
 * @module
 */
public class DimensionFilter {
    /**
     * Hint key for specifying a particular instance of {@code DimensionFilter} to use.
     *
     * @see #getInstance
     *
     * @since 2.5
     */
    public static final Hints.Key INSTANCE = new Hints.Key(DimensionFilter.class);

    /**
     * The input dimensions to keep, in strictly increasing order.
     * This sequence can contains any integers in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive.
     */
    private int[] sourceDimensions;

    /**
     * The output dimensions to keep, in strictly increasing order.
     * This sequence can contains any integers in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     * exclusive.
     */
    private int[] targetDimensions;

    /**
     * The factory for the creation of new math transforms.
     */
    private final MathTransformFactory factory;

    /**
     * Constructs a dimension filter with the default math transform factory.
     */
    public DimensionFilter() {
        this(FactoryFinder.getMathTransformFactory(null));
    }

    /**
     * Constructs a dimension filter with a math transform factory built using the provided hints.
     *
     * @param hints Hints to control the creation of the {@link MathTransformFactory}.
     */
    public DimensionFilter(final Hints hints) {
        this(FactoryFinder.getMathTransformFactory(hints));
    }

    /**
     * Constructs a dimension filter with the specified factory.
     *
     * @param factory The factory for the creation of new math transforms.
     */
    public DimensionFilter(final MathTransformFactory factory) {
        this.factory = factory;
    }

    /**
     * Creates or returns an existing instance from the given set of hints. If the hints contain
     * a value for the {@link #INSTANCE} key, this value is returned. Otherwise a new instance is
     * {@linkplain #DimensionFilter(Hints) created} with the given hints.
     *
     * @param  hints The hints, or {@code null} if none.
     * @return An existing or a new {@code DimensionFilter} instance (never {@code null}).
     *
     * @see #INSTANCE
     *
     * @since 2.5
     */
    public static DimensionFilter getInstance(final Hints hints) {
        if (hints != null) {
            final DimensionFilter candidate = (DimensionFilter) hints.get(INSTANCE);
            if (candidate != null) {
                candidate.clear();
                return candidate;
            }
        }
        return new DimensionFilter(hints);
    }

    /**
     * Clears any {@linkplain #getSourceDimensions source} and
     * {@linkplain #getTargetDimensions target dimension} setting.
     */
    public void clear() {
        sourceDimensions = null;
        targetDimensions = null;
    }

    /**
     * Adds an input dimension to keep. The {@code dimension} applies to the source dimensions
     * of the transform to be given to <code>{@linkplain #separate separate}(transform)</code>.
     * The number must be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive.
     *
     * @param  dimension The dimension to add.
     * @throws IllegalArgumentException if {@code dimension} is negative.
     */
    public void addSourceDimension(final int dimension) throws IllegalArgumentException {
        sourceDimensions = add(sourceDimensions, dimension);
    }

    /**
     * Adds input dimensions to keep. The {@code dimensions} apply to the source dimensions
     * of the transform to be given to <code>{@linkplain #separate separate}(transform)</code>.
     * All numbers must be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>
     * exclusive. The {@code dimensions} values must be in strictly increasing order.
     *
     * @param  dimensions The new sequence of dimensions.
     * @throws IllegalArgumentException if {@code dimensions} contains negative values or
     *         is not a strictly increasing sequence.
     */
    public void addSourceDimensions(final int... dimensions) throws IllegalArgumentException {
        sourceDimensions = add(sourceDimensions, dimensions);
    }

    /**
     * Adds a range of input dimensions to keep. The {@code lower} and {@code upper} values
     * apply to the source dimensions of the transform to be given to
     * <code>{@linkplain #separate separate}(transform)</code>.
     *
     * @param lower The lower dimension, inclusive. Must not be smaller than 0.
     * @param upper The upper dimension, exclusive. Must not be greater than
     * <code>transform.{@linkplain MathTransform#getSourceDimensions getSourceDimensions()}</code>.
     * @throws IllegalArgumentException if {@code lower} or {@code upper} are out of bounds.
     */
    public void addSourceDimensionRange(final int lower, final int upper)
            throws IllegalArgumentException
    {
        sourceDimensions = add(sourceDimensions, lower, upper);
    }

    /**
     * Returns the input dimensions. This information is available only if at least one
     * setter method has been explicitly invoked for source dimensions.
     *
     * @return The input dimension as a sequence of strictly increasing values.
     * @throws IllegalStateException if input dimensions have not been set.
     */
    public int[] getSourceDimensions() throws IllegalStateException {
        if (sourceDimensions != null) {
            return sourceDimensions.clone();
        }
        throw new IllegalStateException();
    }

    /**
     * Adds an output dimension to keep. The {@code dimension} applies to the target dimensions
     * of the transform to be given to <code>{@linkplain #separate separate}(transform)</code>.
     * The number must be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     * exclusive.
     *
     * @param  dimension The dimension to add.
     * @throws IllegalArgumentException if {@code dimension} is negative.
     */
    public void addTargetDimension(final int dimension) throws IllegalArgumentException {
        targetDimensions = add(targetDimensions, dimension);
    }

    /**
     * Adds output dimensions to keep. The {@code dimensions} apply to the target dimensions
     * of the transform to be given to <code>{@linkplain #separate separate}(transform)</code>.
     * All numbers must be in the range 0 inclusive to
     * <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>
     * exclusive. The {@code dimensions} values must be in strictly increasing order.
     *
     * @param  dimensions The new sequence of dimensions.
     * @throws IllegalArgumentException if {@code dimensions} contains negative values or
     *         is not a strictly increasing sequence.
     */
    public void addTargetDimensions(int... dimensions) throws IllegalArgumentException {
        targetDimensions = add(targetDimensions, dimensions);
    }

    /**
     * Adds a range of output dimensions to keep. The {@code lower} and {@code upper} values
     * apply to the target dimensions of the transform to be given to
     * <code>{@linkplain #separate separate}(transform)</code>.
     *
     * @param lower The lower dimension, inclusive. Must not be smaller than 0.
     * @param upper The upper dimension, exclusive. Must not be greater than
     * <code>transform.{@linkplain MathTransform#getTargetDimensions getTargetDimensions()}</code>.
     * @throws IllegalArgumentException if {@code lower} or {@code upper} are out of bounds.
     */
    public void addTargetDimensionRange(final int lower, final int upper)
            throws IllegalArgumentException
    {
        targetDimensions = add(targetDimensions, lower, upper);
    }

    /**
     * Returns the output dimensions. This information is available only if one of the following
     * conditions is meet:
     * <p>
     * <ul>
     *   <li>Target dimensions has been explicitly set using setter methods.</li>
     *   <li>No target dimensions were set but <code>{@linkplain #separate separate}(transform)</code>
     *       has been invoked at least once, in which case the target dimensions are inferred
     *       automatically from the {@linkplain #getSourceDimensions source dimensions} and the
     *       {@code transform}.</li>
     * </ul>
     *
     * @return The output dimension as a sequence of strictly increasing values.
     * @throws IllegalStateException if this information is not available.
     */
    public int[] getTargetDimensions() throws IllegalStateException {
        if (targetDimensions != null) {
            return targetDimensions.clone();
        }
        throw new IllegalStateException();
    }

    /**
     * Separates the specified math transform. This method returns a math transform that uses
     * only the specified {@linkplain #getSourceDimensions source dimensions} and returns only
     * the specified {@linkplain #getTargetDimensions target dimensions}. Special case:
     * <p>
     * <ul>
     *   <li><p>If {@linkplain #getSourceDimensions source dimensions} are unspecified, then the
     *       returned transform will expects all source dimensions as input but will produces only
     *       the specified {@linkplain #getTargetDimensions target dimensions} as output.</p></li>
     *
     *   <li><p>If {@linkplain #getTargetDimensions target dimensions} are unspecified, then the
     *       returned transform will expects only the specified {@linkplain #getSourceDimensions
     *       source dimensions} as input, and the target dimensions will be inferred
     *       automatically.</p></li>
     * </ul>
     *
     * @param  transform The transform to separate.
     * @return The separated math transform.
     * @throws FactoryException if the transform can't be separated.
     */
    public MathTransform separate(MathTransform transform) throws FactoryException {
        /*
         * -------- HACK BEGINS --------
         * Special case for NetCDF transforms. Actually we should generalize the approach used
         * here by providing an abstract protected method in AbstractMathTransform which expect
         * a DimensionFilter (maybe to be renamed) in argument. The default implementation would
         * check for the trivial case allowing to return 'this', and throw an exception for non-
         * trivial cases. Appropriate subclasses (PassthroughTransform, ConcatenatedTransform,
         * etc.) should implement that method.
         */
        if (transform instanceof SeparableTransform) {
            final MathTransform candidate = ((SeparableTransform) transform).subTransform(sourceDimensions, targetDimensions);
            if (candidate != null) {
                // BAD HACK - Presume that source and target dimensions are the same.
                // This is often the case with NetCDF files, but is not garanteed.
                if (sourceDimensions == null) sourceDimensions = targetDimensions;
                if (targetDimensions == null) targetDimensions = sourceDimensions;
                return candidate;
            }
        }
        /*
         * -------- END OF HACK --------
         */
        if (sourceDimensions == null) {
            sourceDimensions = series(0, transform.getSourceDimensions());
            if (targetDimensions == null) {
                targetDimensions = series(0, transform.getTargetDimensions());
                return transform;
            }
            return separateOutput(transform);
        }
        final int[] target = targetDimensions;
        transform = separateInput(transform);
        assert ArraysExt.isSorted(targetDimensions, true);
        if (target != null) {
            final int[] step = targetDimensions;
            targetDimensions = new int[target.length];
            for (int i=0; i<target.length; i++) {
                final int j = Arrays.binarySearch(step, target[i]);
                if (j < 0) {
                    /*
                     * The user is asking for some target dimensions that we can't keep, probably
                     * because at least one of the requested target dimension has a dependency to
                     * a source dimension that doesn't appear in the list of source dimensions to
                     * kept.
                     *
                     * TODO: provide a more accurate error message.
                     */
                    throw new FactoryException(Errors.format(Errors.Keys.INSEPARABLE_TRANSFORM));
                }
                targetDimensions[i] = j;
            }
            transform = separateOutput(transform);
            targetDimensions = target;
        }
        assert sourceDimensions.length == transform.getSourceDimensions() : transform;
        assert targetDimensions.length == transform.getTargetDimensions() : transform;
        return transform;
    }

    /**
     * Separates the math transform on the basis of {@linkplain #sourceDimensions input dimensions}.
     * The remaining {@linkplain #targetDimensions output dimensions} will be selected automatically
     * according the specified input dimensions.
     *
     * @param  transform The transform to reduces.
     * @return A transform expecting only the specified input dimensions.
     * @throws FactoryException if the transform is not separable.
     */
    private MathTransform separateInput(final MathTransform transform) throws FactoryException {
        /*
         * -------- HACK BEGINS -------- (same than in 'separate(...)')
         */
        if (transform instanceof SeparableTransform) {
            final MathTransform candidate = ((SeparableTransform) transform).subTransform(sourceDimensions, targetDimensions);
            if (candidate != null) {
                if (sourceDimensions == null) sourceDimensions = targetDimensions;
                if (targetDimensions == null) targetDimensions = sourceDimensions;
                return candidate;
            }
        }
        /*
         * -------- END OF HACK --------
         */
        final int dimSource = transform.getSourceDimensions();
        final int dimTarget = transform.getTargetDimensions();
        final int dimInput  = sourceDimensions.length;
        final int lower     = sourceDimensions[0];
        final int upper     = sourceDimensions[dimInput-1] + 1;
        assert ArraysExt.isSorted(sourceDimensions, true);
        if (upper > dimSource) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.ILLEGAL_ARGUMENT_2, "sourceDimensions", upper-1));
        }
        /*
         * Check for easiest cases: same transform, identity transform or concatenated transforms.
         */
        if (dimInput == dimSource) {
            assert lower == 0 && upper == dimSource;
            targetDimensions = series(0, dimTarget);
            return transform;
        }
        if (transform.isIdentity()) {
            targetDimensions = sourceDimensions;
            return factory.createAffineTransform(Matrices.createIdentity(dimInput + 1));
        }
        if (Accessor.isConcatenatedTransform(transform)) {
            final int[] original = sourceDimensions;
            final MathTransform step1, step2;
            step1 = separateInput(Accessor.transform1(transform)); sourceDimensions = targetDimensions;
            step2 = separateInput(Accessor.transform2(transform)); sourceDimensions = original;
            return factory.createConcatenatedTransform(step1, step2);
        }
        /*
         * Special case for the pass through transform:  if at least one input dimension
         * belong to the passthrough's sub-transform, then delegates part of the work to
         * {@code subTransform(passThrough.transform, ...)}
         */
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            final int dimPass  = passThrough.getSubTransform().getSourceDimensions();
            final int dimDiff  = passThrough.getSubTransform().getTargetDimensions() - dimPass;
            final int subLower = Accessor.firstAffectedOrdinate(passThrough);
            final int subUpper = subLower + dimPass;
            final DimensionFilter subFilter = new DimensionFilter(factory);
            for (int i=0; i<sourceDimensions.length; i++) {
                int n = sourceDimensions[i];
                if (n >= subLower && n < subUpper) {
                    // Dimension n belong to the subtransform.
                    subFilter.addSourceDimension(n - subLower);
                } else {
                    // Dimension n belong to heading or trailing dimensions.
                    // Passthrough, after adjustment for trailing dimensions.
                    if (n >= subUpper) {
                        n += dimDiff;
                    }
                    targetDimensions = add(targetDimensions, n);
                }
            }
            if (subFilter.sourceDimensions == null) {
                /*
                 * No source dimensions belong to the sub-transform. The only remaining
                 * sources are heading and trailing dimensions. A passthrough transform
                 * without its sub-transform is an identity transform...
                 */
                return factory.createAffineTransform(Matrices.createIdentity(dimInput + 1));
            }
            /*
             * There is at least one dimension to separate in the sub-transform. Performs this
             * separation and gets the list of output dimensions. We need to offset the output
             * dimensions by the amount of leading dimensions once the separation is done, in
             * order to translate from the sub-transform's dimension numbering to the transform's
             * numbering.
             */
            final MathTransform subTransform = subFilter.separateInput(passThrough.getSubTransform());
            for (int i=0; i<subFilter.targetDimensions.length; i++) {
                subFilter.targetDimensions[i] += subLower;
            }
            targetDimensions = add(targetDimensions, subFilter.targetDimensions);
            /*
             * If all source dimensions not in the sub-transform are consecutive numbers, we can
             * use our pass though transform implementation. The "consecutive numbers" requirement
             * (expressed in the 'if' statement below) is a consequence of a limitation in our
             * current implementation: our pass through transform doesn't accept arbitrary index
             * for modified ordinates.
             */
            if (containsAll(sourceDimensions, lower, subLower) &&
                containsAll(sourceDimensions, subUpper, upper))
            {
                final int firstAffectedOrdinate = Math.max(0, subLower-lower);
                final int  numTrailingOrdinates = Math.max(0, upper-subUpper);
                return factory.createPassThroughTransform(
                        firstAffectedOrdinate, subTransform, numTrailingOrdinates);
            }
            // TODO: handle more general case here...
            targetDimensions = null; // Clear before to fallback on the LinearTransform case.
        }
        /*
         * If the transform is affine (or at least projective), express the transform as a matrix.
         * Then, select output dimensions that depends only on selected input dimensions. If an
         * output dimension depends on at least one discarded input dimension, then this output
         * dimension will be discarded as well.
         */
        if (transform instanceof LinearTransform) {
            int           nRows = 0;
            boolean  hasLastRow = false;
            final Matrix matrix = ((LinearTransform) transform).getMatrix();
            assert dimSource+1 == matrix.getNumCol() &&
                   dimTarget+1 == matrix.getNumRow() : matrix;
            double[][] rows = new double[dimTarget+1][];
reduce:     for (int j=0; j<rows.length; j++) {
                final double[] row = new double[dimInput+1];
                /*
                 * For each output dimension (i.e. a matrix row), find the matrix elements for
                 * each input dimension to be kept. If a dependance to at least one discarded
                 * input dimension is found, then the whole output dimension is discarded.
                 *
                 * NOTE: The following loop stops at matrix.getNumCol()-1 because we don't
                 *       want to check the translation term.
                 */
                int nCols=0, scan=0;
                for (int i=0; i<dimSource; i++) {
                    final double element = matrix.getElement(j,i);
                    if (scan < sourceDimensions.length && sourceDimensions[scan] == i) {
                        row[nCols++] = element;
                        scan++;
                    } else if (element != 0) {
                        // Output dimension 'j' depends on one of discarded input dimension 'i'.
                        // The whole row will be discarded.
                        continue reduce;
                    }
                }
                row[nCols++] = matrix.getElement(j, dimSource); // Copy the translation term.
                assert nCols == row.length : nCols;
                if (j == dimTarget) {
                    hasLastRow = true;
                } else {
                    targetDimensions = add(targetDimensions, j);
                }
                rows[nRows++] = row;
            }
            rows = ArraysExt.resize(rows, nRows);
            if (hasLastRow) {
                return factory.createAffineTransform(new GeneralMatrix(rows));
            }
            // In an affine transform,  the last row is not supposed to have dependency
            // to any input dimension. But in this particuler case, our matrix has such
            // dependencies. TODO: is there anything we could do about that?
        }
        throw new FactoryException(Errors.format(Errors.Keys.INSEPARABLE_TRANSFORM));
    }

    /**
     * Creates a transform which retains only a subset of an other transform outputs. The number
     * and nature of inputs stay unchanged. For example if the supplied {@code transform} has
     * (<var>longitude</var>, <var>latitude</var>, <var>height</var>) outputs, then a sub-transform
     * may be used to keep only the (<var>longitude</var>, <var>latitude</var>) part. In most cases,
     * the created sub-transform is non-invertible since it loose informations.
     * <p>
     * This transform may be see as a non-square matrix transform with less rows
     * than columns, concatenated with {@code transform}. However, invoking
     * {@code createFilterTransfom(...)} allows the optimization of some common cases.
     *
     * @param  transform The transform to reduces.
     * @return The {@code transform} keeping only the output dimensions.
     * @throws FactoryException if the transform can't be created.
     */
    private MathTransform separateOutput(MathTransform transform) throws FactoryException {
        final int dimSource = transform.getSourceDimensions();
        final int dimTarget = transform.getTargetDimensions();
        final int dimOutput = targetDimensions.length;
        final int lower     = targetDimensions[0];
        final int upper     = targetDimensions[dimOutput-1];
        assert ArraysExt.isSorted(targetDimensions, true);
        if (upper > dimTarget) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.ILLEGAL_ARGUMENT_2, "targetDimensions", upper));
        }
        if (dimOutput == dimTarget) {
            assert lower==0 && upper==dimTarget;
            return transform;
        }
        /*
         * If the transform is an instance of "pass through" transform but no dimension from its
         * subtransform is requested, then ignore the subtransform (i.e. treat the whole transform
         * as identity, except for the number of output dimension which may be different from the
         * number of input dimension).
         */
        int dimPass = 0;
        int dimDiff = 0;
        int dimStep = dimTarget;
        if (transform instanceof PassThroughTransform) {
            final PassThroughTransform passThrough = (PassThroughTransform) transform;
            final int subLower = Accessor.firstAffectedOrdinate(passThrough);
            final int subUpper = subLower + passThrough.getSubTransform().getTargetDimensions();
            if (!containsAny(targetDimensions, subLower, subUpper)) {
                transform = null;
                dimStep = dimSource;
                dimPass = subLower;
                dimDiff = (subLower + passThrough.getSubTransform().getSourceDimensions()) - subUpper;
            }
        }
        /*                                                  ┌  ┐     ┌          ┐ ┌ ┐
         * Creates the matrix to be used as a filter,       │x'│     │1  0  0  0│ │x│
         * and concatenates it to the transform. The        │z'│  =  │0  0  1  0│ │y│
         * matrix will contains only a 1 for the output     │1 │     │0  0  0  1│ │z│
         * dimension to keep, as in the following example:  └  ┘     └          ┘ │1│
         *                                                                        └ ┘
         */
        final Matrix matrix = Matrices.createZero(dimOutput+1, dimStep+1);
        for (int j=0; j<dimOutput; j++) {
            int i = targetDimensions[j];
            if (i >= dimPass) {
                i += dimDiff;
            }
            matrix.setElement(j, i, 1);
        }
        // Affine transform has one more row/column than dimension.
        matrix.setElement(dimOutput, dimStep, 1);
        MathTransform filtered = factory.createAffineTransform(matrix);
        if (transform != null) {
            filtered = factory.createConcatenatedTransform(transform, filtered);
        }
        return filtered;
    }

    /**
     * Returns {@code true} if the given sequence contains all index in the range {@code lower}
     * inclusive to {@code upper} exclusive.
     *
     * @param  sequence The {@link #sourceDimensions} or {@link #targetDimensions} sequence to test.
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, exclusive.
     * @return {@code true} if the full range was found in the sequence.
     */
    private static boolean containsAll(final int[] sequence, final int lower, int upper) {
        if (lower == upper) {
            return true;
        }
        if (sequence != null) {
            assert ArraysExt.isSorted(sequence, true);
            int index = Arrays.binarySearch(sequence, lower);
            if (index >= 0) {
                index += --upper - lower;
                if (index >= 0 && index < sequence.length) {
                    return sequence[index] == upper;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the given sequence contains any value in the given range.
     *
     * @param  sequence The {@link #sourceDimensions} or {@link #targetDimensions} sequence to test.
     * @param  lower The lower value, inclusive.
     * @param  upper The upper value, exclusive.
     * @return {@code true} if the sequence contains at least one value in the given range.
     */
    private static boolean containsAny(final int[] sequence, final int lower, final int upper) {
        if (upper == lower) {
            return true;
        }
        if (sequence != null) {
            assert ArraysExt.isSorted(sequence, true);
            int index = Arrays.binarySearch(sequence, lower);
            if (index >= 0) {
                return true;
            }
            index = ~index; // Tild, not minus sign.
            return index < sequence.length  &&  sequence[index] < upper;
        }
        return false;
    }

    /**
     * Adds the specified {@code dimension} to the specified sequence. Values are added
     * in increasing order. Duplicated values are not added.
     *
     * @param sequence The {@link #sourceDimensions} or {@link #targetDimensions} sequence to update.
     */
    private static int[] add(int[] sequence, int dimension) throws IllegalArgumentException {
        if (dimension < 0) {
            throw new IllegalArgumentException(Errors.format(
                    Errors.Keys.ILLEGAL_ARGUMENT_2, "dimension", dimension));
        }
        if (sequence == null) {
            return new int[] {dimension};
        }
        assert ArraysExt.isSorted(sequence, true);
        int i = Arrays.binarySearch(sequence, dimension);
        if (i < 0) {
            i = ~i;   // Tild, not the minus sign.
            sequence = ArraysExt.insert(sequence, i, 1);
            sequence[i] = dimension;
        }
        assert Arrays.binarySearch(sequence, dimension) == i;
        return sequence;
    }

    /**
     * Adds the specified {@code dimensions} to the specified sequence. Values are added
     * in increasing order. Duplicated values are not added.
     *
     * @param sequence The {@link #sourceDimensions} or {@link #targetDimensions} sequence to update.
     */
    private static int[] add(int[] sequence, final int[] dimensions)
            throws IllegalArgumentException
    {
        if (dimensions.length != 0) {
            ensureValidSeries(dimensions);
            if (sequence == null) {
                sequence = dimensions.clone();
            } else {
                // Note: the following loop is unefficient, but should suffise since this
                //       case should not occurs often and arrays should be small anyway.
                for (int i=0; i<dimensions.length; i++) {
                    sequence = add(sequence, dimensions[i]);
                }
            }
        }
        return sequence;
    }

    /**
     * Adds the specified range to the specified sequence. Values are added
     * in increasing order. Duplicated values are not added.
     *
     * @param sequence The {@link #sourceDimensions} or {@link #targetDimensions} sequence to update.
     * @throws IllegalArgumentException if {@code lower} is not smaller than {@code upper}.
     */
    private static int[] add(int[] sequence, int lower, final int upper)
            throws IllegalArgumentException
    {
        if (lower<0 || lower>=upper) {
            throw new IllegalArgumentException(Errors.format(Errors.Keys.ILLEGAL_RANGE_2, lower, upper));
        }
        if (sequence == null) {
            sequence = series(lower, upper);
        } else {
            // Note: the following loop is unefficient, but should suffise since this
            //       case should not occurs often and arrays should be small anyway.
            while (lower < upper) {
                sequence = add(sequence, lower++);
            }
        }
        assert containsAll(sequence, lower, upper);
        return sequence;
    }

    /**
     * Returns a series of increasing values starting at {@code lower}.
     */
    private static int[] series(final int lower, final int upper) throws IllegalArgumentException {
        final int[] sequence = new int[upper-lower];
        for (int i=0; i<sequence.length; i++) {
            sequence[i] = i+lower;
        }
        return sequence;
    }

    /**
     * Ensures that the specified array contains strictly increasing non-negative values.
     *
     * @param  dimensions The sequence to check.
     * @throws IllegalArgumentException if the specified sequence is not a valid series.
     */
    private static void ensureValidSeries(final int[] dimensions) throws IllegalArgumentException {
        int last = -1;
        for (int i=0; i<dimensions.length; i++) {
            final int value = dimensions[i];
            if (value <= last) {
                throw new IllegalArgumentException(Errors.format(
                        Errors.Keys.ILLEGAL_ARGUMENT_2, "dimensions[" + i + ']', value));
            }
            last = value;
        }
    }
}

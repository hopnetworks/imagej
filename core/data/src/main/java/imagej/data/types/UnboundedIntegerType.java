/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.data.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.imglib2.type.numeric.IntegerType;

/**
 * @author Barry DeZonia
 */
public class UnboundedIntegerType implements IntegerType<UnboundedIntegerType> {

	// -- fields --

	private BigInteger v = BigInteger.ZERO;

	// -- accessors --

	public BigInteger get() {
		return v;
	}

	public void set(BigInteger val) {
		v = val;
	}

	// -- NumericType methods --

	@Override
	public UnboundedIntegerType createVariable() {
		return new UnboundedIntegerType();
	}

	@Override
	public UnboundedIntegerType copy() {
		UnboundedIntegerType copy = new UnboundedIntegerType();
		copy.v = this.v;
		return copy;
	}

	@Override
	public void set(UnboundedIntegerType c) {
		this.v = c.v;
	}

	@Override
	public void add(UnboundedIntegerType c) {
		this.v = v.add(c.v);
	}

	@Override
	public void sub(UnboundedIntegerType c) {
		this.v = v.subtract(c.v);
	}

	@Override
	public void mul(UnboundedIntegerType c) {
		this.v = v.multiply(c.v);
	}

	@Override
	public void div(UnboundedIntegerType c) {
		this.v = v.divide(c.v);
	}

	@Override
	public void setZero() {
		this.v = BigInteger.ZERO;
	}

	@Override
	public void setOne() {
		this.v = BigInteger.ONE;
	}

	@Override
	public void mul(float c) {
		doMul(new BigDecimal(c));
	}

	@Override
	public void mul(double c) {
		doMul(new BigDecimal(c));
	}

	// -- helpers --

	private void doMul(BigDecimal factor) {
		BigDecimal val = new BigDecimal(this.v);
		BigDecimal result = val.multiply(factor);
		this.v = result.toBigInteger();
	}

	// -- required IntegerType methods
	// Note that most of them are precision losing methods

	@Override
	public void inc() {
		this.v = this.v.add(BigInteger.ONE);
	}

	@Override
	public void dec() {
		this.v = this.v.subtract(BigInteger.ONE);
	}

	@Override
	public double getMaxValue() {
		return Double.MAX_VALUE; // TODO - narrowing!
	}

	@Override
	public double getMinValue() {
		return -Double.MAX_VALUE; // TODO - narrowing!
	}

	@Override
	public double getMinIncrement() {
		return Double.MIN_VALUE; // TODO - narrowing
	}

	@Override
	public int getBitsPerPixel() {
		return 1024; // TODO arbitrary
	}

	@Override
	public double getRealDouble() {
		return this.v.doubleValue();
	}

	@Override
	public float getRealFloat() {
		return this.v.floatValue();
	}

	@Override
	public double getImaginaryDouble() {
		return 0;
	}

	@Override
	public float getImaginaryFloat() {
		return 0;
	}

	@Override
	public void setReal(float f) {
		this.v = BigInteger.valueOf((long) f);
	}

	@Override
	public void setReal(double f) {
		this.v = BigInteger.valueOf((long) f);
	}

	@Override
	public void setImaginary(float f) {
		// do nothing
		// TODO - throw except if f != 0 ?
	}

	@Override
	public void setImaginary(double f) {
		// do nothing
		// TODO - throw except if f != 0 ?
	}

	@Override
	public void setComplexNumber(float r, float i) {
		setReal(r);
		// TODO - throw except if i != 0 ?
	}

	@Override
	public void setComplexNumber(double r, double i) {
		setReal(r);
		// TODO - throw except if i != 0 ?
	}

	@Override
	public float getPowerFloat() {
		return this.v.floatValue();
	}

	@Override
	public double getPowerDouble() {
		return this.v.doubleValue();
	}

	@Override
	public float getPhaseFloat() {
		return 0;
	}

	@Override
	public double getPhaseDouble() {
		return 0;
	}

	@Override
	public void complexConjugate() {
		// do nothing
	}

	@Override
	public int compareTo(UnboundedIntegerType o) {
		return this.v.compareTo(o.v);
	}

	@Override
	public int getInteger() {
		return this.v.intValue();
	}

	@Override
	public long getIntegerLong() {
		return this.v.longValue();
	}

	@Override
	public void setInteger(int f) {
		this.v = BigInteger.valueOf(f);
	}

	@Override
	public void setInteger(long f) {
		this.v = BigInteger.valueOf(f);
	}
}

package org.bdgp.util;

public class EqualsEqualityComparator implements EqualityComparator {
    public boolean equals(Object a, Object b) {
	return ObjectUtil.equals(a, b);
    }
}

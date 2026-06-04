package io.github.imecuadorian.smartguardbackend.monitoring.domain;

import java.math.BigDecimal;

public enum ComparisonOperator {
    GREATER_THAN {
        @Override
        public boolean matches(BigDecimal actual, BigDecimal expected) {
            return actual.compareTo(expected) > 0;
        }
    },
    GREATER_THAN_OR_EQUAL {
        @Override
        public boolean matches(BigDecimal actual, BigDecimal expected) {
            return actual.compareTo(expected) >= 0;
        }
    },
    LESS_THAN {
        @Override
        public boolean matches(BigDecimal actual, BigDecimal expected) {
            return actual.compareTo(expected) < 0;
        }
    },
    LESS_THAN_OR_EQUAL {
        @Override
        public boolean matches(BigDecimal actual, BigDecimal expected) {
            return actual.compareTo(expected) <= 0;
        }
    },
    EQUAL {
        @Override
        public boolean matches(BigDecimal actual, BigDecimal expected) {
            return actual.compareTo(expected) == 0;
        }
    };

    public abstract boolean matches(BigDecimal actual, BigDecimal expected);
}

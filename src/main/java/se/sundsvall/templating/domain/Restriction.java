package se.sundsvall.templating.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
/*
 * TODO: should this class even exist ?? will we ever have the need to combine key-values ??
 * TODO: if so, also remove the @Generated annotations below that are only there to exclude from coverage...
 */
@Generated
public interface Restriction {

    /**
     * Returns a new {@code Restriction} logically combining the given ones using AND.
     *
     * @param restrictions the restrictions to combine using AND
     * @return a new {@code Restriction}
     */
    static Restriction and(final Restriction...restrictions) {
        return new And(restrictions);
    }

    /**
     * Returns a new {@code Restriction} logically combining the given ones using OR.
     *
     * @param restrictions the restrictions to combine using OR
     * @return a new {@code Restriction}
     */
    static Restriction or(final Restriction...restrictions) {
        return new Or(restrictions);
    }

    /**
     * Returns a new {@code Restriction} matching the given key and value.
     *
     * @param key the key
     * @param value the value
     * @return a new {@code Restriction}
     */
    static Restriction equalTo(final String key, final String value) {
        return KeyValue.equalTo(key, value);
    }

    @Generated
    @Getter
    abstract class Junction implements Restriction {

        private final List<Restriction> restrictions = new ArrayList<>();

        protected Junction(final List<Restriction> restrictions) {
            this.restrictions.addAll(restrictions);
        }
    }

    @Generated
    class And extends Junction {

        public And(final Restriction...clauses) {
            super(Arrays.asList(clauses));
        }
    }

    @Generated
    class Or extends Junction {

        public Or(final Restriction...restrictions) {
            super(Arrays.asList(restrictions));
        }
    }

    @Generated
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    class KeyValue implements Restriction {

        private final String key;
        private final String value;

        public static Restriction equalTo(final String key, final String value) {
            return new KeyValue(key, value);
        }
    }
}

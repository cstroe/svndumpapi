package com.github.cstroe.svndumpgui.internal.utility;

import java.util.Objects;

public class Tuple2<FIRST, SECOND> {
    public final FIRST _1;
    public final SECOND _2;

    private Tuple2(FIRST _1, SECOND _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public static <FIRST, SECOND> Tuple2<FIRST, SECOND> of(FIRST _1, SECOND _2) {
        return new Tuple2<>(_1, _2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1, tuple2._1) && Objects.equals(_2, tuple2._2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    @Override
    public String toString() {
        return "(" + _1 + "," + _2 + ')';
    }
}

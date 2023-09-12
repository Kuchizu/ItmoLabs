<?php
enum Figures {
    case TRIANGLE;
    case SQUARE;
    case CIRCLE;
    case EMPTY;

    public function check() {
        return match ($this) {
            Figures::CIRCLE => function ($x, $y, $r) {
                $katet_sum_x2 = pow($x, 2) + pow($y, 2);
                $gipatinusa = pow($katet_sum_x2, 0.5);
                return $gipatinusa <= $r/2;
            },
            Figures::TRIANGLE => function ($x, $y, $r) {
                $y_koef = 1; // Соотношение катета y  к катету по x в зананном триугольнике
                $x_katet = $r/2 - $x; // При условии, что в заданном триугольнике катет по x равен r
                if ($x_katet < 0) {
                    return false;
                }
                $y_katet = $x_katet * $y_koef;
                return $y <= $y_katet;
            },
            Figures::SQUARE => function ($x, $y, $r) {
                //в зависимости от варианта
                return ($x <= $r) && ($y <= $r);
            },
            Figures::EMPTY => function ($x, $y, $r) {
                return false;
            }
        };
    }
}

enum Quarter {
    case NORTH_WEST;
    case NORTH_EAST;
    case SOUTH_WEST;
    case SOUTH_EAST;

    function findFigure() {
        return match ($this) {
            Quarter::NORTH_EAST => Figures::SQUARE,
            Quarter::NORTH_WEST => Figures::CIRCLE,
            Quarter::SOUTH_EAST => Figures::EMPTY,
            Quarter::SOUTH_WEST => Figures::TRIANGLE
        };
    }
}

function findQuarter($x, $y) {
    if ($x > 0) {
        if ($y < 0) {
            return Quarter::SOUTH_EAST;
        } else {
            return Quarter::NORTH_EAST;
        }
    }
    if ($y < 0) {
        return Quarter::SOUTH_WEST;
    } else {
        return Quarter::NORTH_WEST;
    }
}

?>

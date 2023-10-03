<?php
session_start();
date_default_timezone_set('Europe/Moscow');
?>

<?php
include_once 'enums.php';

if (!isset($_SESSION["result"])) {
    $_SESSION["result"] = array();
}

function validateInput($x, $y, $r) {
    $x = str_replace(',', '.', $x);
    $y = str_replace(',', '.', $y);
    $r = str_replace(',', '.', $r);

    if (!is_numeric($x) || !is_numeric($y) || !is_numeric($r)) {
        return false;
    }



    if ($x < -3 or $x > 5){
        return false;
    }

    if ($y < -3 or $y > 5){
        return false;
    }

    if($r < 1 or $r > 5){
        return false;
    }

    return true;
}

function run($x, $y, $r) {
    $x = str_replace(',', '.', $x);
    $y = str_replace(',', '.', $y);
    $r = str_replace(',', '.', $r);

    $quarter = findQuarter($x, $y);
    $figure = $quarter->findFigure();
    $checker = $figure->check();
    if ($checker(abs($x), abs($y), $r)) {
        return "hit";
    } else {
        return "miss";
    }
}

if (isset($_GET["x"]) && isset($_GET["y"]) && isset($_GET["r"])) {
    $x = $_GET["x"];
    $y = $_GET["y"];
    $r = $_GET["r"];

    if (validateInput($x, $y, $r)) {
        $start_date = new DateTimeImmutable();
        $start = (int)$start_date->format('Uu');
        $is_success = run($x, $y, $r);
        $finish_date = new DateTimeImmutable();
        $finish = (int)$finish_date->format('Uu');
        $dif = $finish - $start;

        $new_data = array(
            "x" => $x,
            "y" => $y,
            "r" => $r,
            "result" => $is_success,
            "current_time" => date("Y-m-d \rG:i:s"),
            "period" => $dif
        );

        array_push($_SESSION["result"], $new_data);
        echo json_encode($_SESSION["result"]);
    }
}
?>
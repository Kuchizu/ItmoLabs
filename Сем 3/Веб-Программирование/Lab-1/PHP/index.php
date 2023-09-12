<?php
session_start();
date_default_timezone_set('Europe/Moscow');
?>
<?php
include_once 'enums.php';

if (!isset($_SESSION["result"])) {
    $_SESSION["result"] = array();
}


function run($x, $y, $r) {
    $quarter = findQuarter($x, $y);
    $figure = $quarter->findFigure();
    $checker = $figure->check();
    if ($checker(abs($x), abs($y), $r)) {
        return "hit";
    } else {
        return "miss";
    }
}

$start_date = new DateTimeImmutable();
$start = (int)$start_date->format('Uu');
$is_success = run($_GET["x"], $_GET["y"], $_GET["r"]);
$finish_date = new DateTimeImmutable();
$finish = (int)$finish_date->format('Uu');
$dif = $finish - $start;

$new_data = array(
    "x"=>$_GET["x"],
    "y"=>$_GET["y"],
    "r"=>$_GET["r"],
    "result"=>$is_success,
    "current_time"=>$today = date("Y-m-d \rG:i:s"),
    "period"=>$dif
);

//file_put_contents(
//    "result.txt",
//    json_encode($new_data).",".PHP_EOL,
//    FILE_APPEND
//);
//
//$result = "[".file_get_contents(
//    "result.txt"
//)."]";
//$result = str_replace(",".PHP_EOL."]", "]", $result);
//echo $result;
array_push($_SESSION["result"], $new_data);
echo json_encode($_SESSION["result"]);
?>

<?php
session_start();
//$result = "[".file_get_contents(
//        "result.txt"
//    )."]";
//$result = str_replace(",".PHP_EOL."]", "]", $result);
//echo $result;

if (!isset($_SESSION["result"])) {
    $_SESSION["result"] = array();
}

echo json_encode($_SESSION["result"]);
?>

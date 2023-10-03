<?php
session_start();
if (!isset($_SESSION["result"])) {
    $_SESSION["result"] = array();
}

echo json_encode($_SESSION["result"]);
?>

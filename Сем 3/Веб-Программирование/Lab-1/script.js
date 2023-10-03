function check_value() {
    if (checkX() && checkY() && checkR()) {
        document.getElementById("sub_but").disabled = false;
        return true;
    }
    else {
        document.getElementById("sub_but").disabled = true;
        return false;
    }
}

function checkY() {
    let y = document.getElementById("y").value;
    y = y.replace(',', '.');
    if (isNaN(y)) {
        document.getElementById("y").classList.add("incorrectY");
        return false;
    }
    if (y == "") {
        document.getElementById("y").classList.add("incorrectY");
        return false;
    }
    if ((y.split('.')[1] || []).length > 4){
        document.getElementById("y").classList.remove("incorrectY");
        return false;
    }
    if ((y >= -3) && (y <= 5)) {
        document.getElementById("y").classList.remove("incorrectY");
        return true;
    }
    else {
        document.getElementById("y").classList.add("incorrectY");
        return false;
    }
}

function checkR() {
        const collection = document.getElementsByClassName("r");
        for (let i = 0; i < collection.length; i++) {
            if (collection[i].checked == true) {
                return true;
            }
        }
        return false;
}

function handleCheckbox(elem) {
    if (elem.checked == true) {
        const collection = document.getElementsByClassName("r");
        for (let i = 0; i < collection.length; i++) {
            if (collection[i] != elem) {
                collection[i].checked = false;
            }
        }
    }
}

function checkX() {
    return document.getElementById("x_val").value !== "";
}

function saveX(x) {
    document.getElementById("x_val").value = x.value;
    x.disabled = true;
    let buttons = document.getElementsByClassName("x");
    for (let i = 0; i < buttons.length; i++) {
        if (buttons[i] !== x) {
            buttons[i].disabled = false;
        }
    }
}

function clear_but() {
    document.getElementById("clear").disabled = document.querySelectorAll('.res_tab').length === 0;
}

$(document).ready( function() {
    $.ajax({
        type: 'GET',
        crossDomain: true,
        dataType: 'json',
        url: 'PHP/load_table.php',
        success: function(data,textStatus,xhr){
            console.log(data);
            var result_str = "<tbody>";
            for (let i = 0; i < data.length; i++) {
                result_str += "<tr class='res_tab'>";
                result_str += "<td>" + data[i].x + "</td>";
                result_str += "<td>" + data[i].y + "</td>";
                result_str += "<td>" + data[i].r + "</td>";
                if (data[i].result === "hit") {
                    result_str += "<td style='color: green;'>" + data[i].result + "</td>";
                } else{
                    result_str += "<td style='color: red;'>" + data[i].result + "</td>";
                }
                result_str += "<td>" + data[i].current_time + "</td>"
                result_str += "<td>" + data[i].period + "</td>"
                result_str += "</tr>";
            }
            result_str += "</tbody>";
            $(".result").html(result_str);
            window.location.replace('index.html#end_table');
            clear_but();
        },
        error: function(xhr, statusText, err) {
            // alert("Error 1: "+statusText+" "+err);
        }
    })

    $("#clear").click(
        function () {
            $.ajax({
                type: 'GET',
                crossDomain: true,
                url: 'PHP/clean_table.php',
                success: function () {
                    let table = document.querySelectorAll('.res_tab');
                    for (let i = 0; i < table.length; i++) {
                        table[i].parentNode.removeChild(table[i]);
                    }
                    clear_but();
                }
            })
        }
    )
    $("#sub_but").click(
        function () {
                var r = $(".r:checked").val();
                var x = $("#x_val").val();
                var y = $("#y").val();
                $.ajax({
                    type: 'GET',
                    crossDomain: true,
                    data: {r:r, x:x, y:y},
                    dataType: 'json',
                    url: 'PHP/index.php',
                    success:
                        function(data,textStatus,xhr){
                            console.log(data);
                            var result_str = "<tbody>";
                            for (let i = 0; i < data.length; i++) {
                                result_str += "<tr class='res_tab'>";
                                result_str += "<td>" + data[i].x + "</td>";
                                result_str += "<td>" + data[i].y + "</td>";
                                result_str += "<td>" + data[i].r + "</td>";
                                if (data[i].result === "hit") {
                                    result_str += "<td style='color: green;'>" + data[i].result + "</td>";
                                } else{
                                    result_str += "<td style='color: red;'>" + data[i].result + "</td>";
                                }
                                result_str += "<td>" + data[i].current_time + "</td>"
                                result_str += "<td>" + data[i].period + "</td>"
                                result_str += "</tr>";
                            }
                            result_str += "</tbody>";
                            $(".result").html(result_str);
                            window.location.replace('index.html#end_table');
                            clear_but();
                        },
                    error: function(xhr,textStatus,errorThrown){
                        console.log('Error Something');
                        console.log(errorThrown);
                        console.log(textStatus);
                    }
                })

    })

})


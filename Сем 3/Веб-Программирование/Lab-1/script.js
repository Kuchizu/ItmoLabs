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

function checkX() {
    let x = document.getElementById("x").value;
    if (isNaN(x)) {
        document.getElementById("x").classList.add("incorrectX");
        return false;
    }
    if (x == "") {
        document.getElementById("x").classList.add("incorrectX");
        return false;
    }
    if ((x >= -5) && (x <= 3)) {
        document.getElementById("x").classList.remove("incorrectX");
        return true;
    }
    else {
        document.getElementById("x").classList.add("incorrectX");
        return false;
    }
}

function checkY() {
        const collection = document.getElementsByClassName("y");
        for (let i = 0; i < collection.length; i++) {
            if (collection[i].checked == true) {
                return true;
            }
        }
        return false;
}

function handleCheckbox(elem) {
    if (elem.checked == true) {
        const collection = document.getElementsByClassName("y");
        for (let i = 0; i < collection.length; i++) {
            if (collection[i] != elem) {
                collection[i].checked = false;
            }
        }
    }
}

function checkR() {
    if (document.getElementById("r_val").value == "") {
        return false;
    }
    return true;
}

function saveR(r) {
    document.getElementById("r_val").value = r.value;
    r.disabled = true;
    let buttons = document.getElementsByClassName("r");
    for (let i = 0; i < buttons.length; i++) {
        if (buttons[i] !== r) {
            buttons[i].disabled = false;
        }
    }
}

function clear_but() {
    if (document.querySelectorAll('.res_tab').length == 0) {
        document.getElementById("clear").disabled = true;
    } else {
        document.getElementById("clear").disabled = false;
    }
}

$(document).ready( function() {
    $.ajax({
        type: 'GET',
        crossDomain: true,
        dataType: 'json',
        url: 'load_table.php',
        success: function(data,textStatus,xhr){
            console.log(data);
            var result_str = "<tbody>";
            for (let i = 0; i < data.length; i++) {
                result_str += "<tr class='res_tab'>";
                result_str += "<td>" + data[i].x + "</td>";
                result_str += "<td>" + data[i].y + "</td>";
                result_str += "<td>" + data[i].r + "</td>";
                if (data[i].result == "hit") {
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
            alert("error: "+statusText+" "+err);
        }
    })

    $("#clear").click(
        function () {
            $.ajax({
                type: 'GET',
                crossDomain: true,
                url: 'clean_table.php',
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
                var r = $("#r_val").val();
                var x = $("#x").val();
                var y = $(".y:checked").val();
                $.ajax({
                    type: 'GET',
                    crossDomain: true,
                    data: {r:r, x:x, y:y},
                    dataType: 'json',
                    url: 'index.php',
                    success:
                        function(data,textStatus,xhr){
                            console.log(data);
                            var result_str = "<tbody>";
                            for (let i = 0; i < data.length; i++) {
                                result_str += "<tr class='res_tab'>";
                                result_str += "<td>" + data[i].x + "</td>";
                                result_str += "<td>" + data[i].y + "</td>";
                                result_str += "<td>" + data[i].r + "</td>";
                                if (data[i].result == "hit") {
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


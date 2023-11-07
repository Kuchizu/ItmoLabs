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
    if (y === "") {
        document.getElementById("y").classList.add("incorrectY");
        return false;
    }
    if ((y.split('.')[1] || []).length > 4){
        document.getElementById("y").classList.remove("incorrectY");
        return false;
    }
    if ((y >= -3) && (y <= 3)) {
        document.getElementById("y").classList.remove("incorrectY");
        return true;
    }
    else {
        document.getElementById("y").classList.add("incorrectY");
        return false;
    }
}

function checkX() {
        const collection = document.getElementsByClassName("x");
        for (let i = 0; i < collection.length; i++) {
            if (collection[i].checked === true) {
                return true;
            }
        }
        return false;
}

function checkR() {
    return document.getElementById("r_val").value !== "";
}

function clearPoints() {
    for (let point of points) {
        calculator.removeExpression({ id: point.x + '' + point.y });
    }
}

function saveR(r) {
    let old_r = document.getElementById("r_val").value
    document.getElementById("r_val").value = r.value;
    drawFig(r.value);

    r.disabled = true;
    let buttons = document.getElementsByClassName("r");
    for (let i = 0; i < buttons.length; i++) {
        if (buttons[i] !== r) {
            buttons[i].disabled = false;
        }
    }
    console.log(old_r.toString() + " " + r.value.toString() + "  //  " + old_r / r.value);

    clearPoints();
    let diff = r.value / old_r;
    for (let point of points){
        point.x = point.x * diff;
        point.y = point.y * diff;
        drawPoint(point.x, point.y);
    }
}

$(document).ready(function() {
    $("#sub_but").submit(function (event) {
        console.log("Button clicked!")
        event.preventDefault();
        const r = $("#r_val").val();
        const x = $(".x:checked").val();
        const y = $("#y").val();
        const formAction = $("#forma").attr("action");

        $.ajax({
            type: 'GET',
            data: { r: r, x: x, y: y },
            dataType: 'html',
            url: formAction,
            success: function(data) {
                let tableBody = $(".result-body");
            },
            error: function (xhr, textStatus, errorThrown) {
                console.log('Error Something');
                console.log(errorThrown);
                console.log(textStatus);
                alert(xhr + textStatus + errorThrown);
            }
        });
    });

    $("#clear_table").click(function () {
        clearPoints();
        points = [];
        const formAction = $("#forma").attr("action");
        $.ajax({
            type: 'GET',
            url: formAction + "?clear=1",
            success: function() {
                let tableBody = $(".result-body");
                tableBody.empty();
            },
            error: function (xhr, textStatus, errorThrown) {
                console.log('Error in clear_table');
                console.log(errorThrown);
                console.log(textStatus);
                alert(xhr + textStatus + errorThrown);
            }
        });
    });
});

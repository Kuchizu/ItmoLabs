let graph_click_enabled = false;
const elt = document.getElementById('calculator');
const calculator = Desmos.GraphingCalculator(elt, {
    keypad: false,
    expressions: false,
    settingsMenu: false,
    invertedColors: true,
    xAxisLabel: 'x',
    yAxisLabel: 'y',
    xAxisStep: 1,
    yAxisStep: 1,
    xAxisArrowMode: Desmos.AxisArrowModes.POSITIVE,
    yAxisArrowMode: Desmos.AxisArrowModes.POSITIVE
});

calculator.setMathBounds({
    left: -4,
    right: 4,
    bottom: -4,
    top: 4
});

let newDefaultState = calculator.getState();
calculator.setDefaultState(newDefaultState);


let points = [];

function drawPoint(x, y) {
    calculator.setExpression({
        id: x + '' + y,
        latex: '(' + x + ', ' + y + ')',
        color: Desmos.Colors.RED
    });
    console.log(points);
}

function drawFig(R){
    calculator.setExpression({ id: 'triangle', latex: `\\polygon((0, 0), (${R/2}, 0), (0, ${-R}))`, color: Desmos.Colors.RED, opacity: 0.3});
    calculator.setExpression({ id: 'rectangle', latex: `\\polygon((${-R}, 0), (0, 0), (0, ${-R}), (${-R}, ${-R}))`, color: Desmos.Colors.RED, opacity: 0.3});
    calculator.setExpression({id: 'circle', latex: `r<=${R} \\{\\frac{\\pi}{2}\\le\\theta\\le\\pi\\}`, color: Desmos.Colors.RED});
}
function enable_graph() {
    if (graph_click_enabled) {
        elt.removeEventListener('click', handleGraphClick);
        graph_click_enabled = false;
    } else {
        elt.addEventListener('click', handleGraphClick);
        graph_click_enabled = true;
    }
}

function handleGraphClick (evt) {
    const r_val = $("#r_val").val();
    if (r_val == null || r_val === "") {
        alert("Choose R!");
        return;
    }

    const rect = elt.getBoundingClientRect();
    const x = evt.clientX - rect.left;
    const y = evt.clientY - rect.top;

    // Note, pixelsToMath expects x and y to be referenced to the top left of
    // the calculator's parent container.
    const mathCoordinates = calculator.pixelsToMath({x: x, y: y});

    console.log('setting expression...');
    console.log(mathCoordinates);

    points.push({x: mathCoordinates.x, y: mathCoordinates.y});
    drawPoint(mathCoordinates.x, mathCoordinates.y);

    $.ajax({
        type: 'GET',
        data: { r: r_val, x: mathCoordinates.x, y: mathCoordinates.y, m: true },
        dataType: 'html',
        url: $("#forma").attr("action"),
        success: function(data) {
            let tableBody = $(".result-body");
            tableBody.append(data);
        },
        error: function (xhr, textStatus, errorThrown) {
            console.log('Error Something');
            console.log(errorThrown);
            console.log(textStatus);
            alert(xhr + textStatus + errorThrown);
        }
    });
}
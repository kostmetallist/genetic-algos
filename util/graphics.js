window.addEventListener('DOMContentLoaded', () => {

    // used: www.speqmath.com/tutorials/csv2array/
    function csv2array(data, delimeter) {

        // Retrieve the delimeter
        if (delimeter == undefined) 
            delimeter = ',';

        if (delimeter && delimeter.length > 1)
            delimeter = ',';

        // initialize variables
        var newline = '\n';
        var eof = '';
        var i = 0;
        var c = data.charAt(i);
        var row = 0;
        var col = 0;
        var array = new Array();

        while (c != eof) {

            // skip whitespaces
            while (c == ' ' || c == '\t' || c == '\r') {
                c = data.charAt(++i); // read next char
            }

            // get value
            var value = "";
            if (c == '\"') {
                // value enclosed by double-quotes
                c = data.charAt(++i);
              
                do {

                    if (c != '\"') {
                        // read a regular character and go to 
                        // the next character
                        value += c;
                        c = data.charAt(++i);
                    }
                
                    if (c == '\"') {
                        // check for escaped double-quote
                        var cnext = data.charAt(i+1);
                        if (cnext == '\"') {
                            // this is an escaped double-quote. 
                            // Add a double-quote to the value, and 
                            // move two characters ahead.
                            value += '\"';
                            i += 2;
                            c = data.charAt(i);
                        }
                    }
                } while (c != eof && c != '\"');
              
                if (c == eof) {
                    throw "Unexpected end of data, double-quote expected";
                }

                c = data.charAt(++i);
            }

            else {
                // value without quotes
                while (c != eof && c != delimeter && c!= newline && 
                    c != ' ' && c != '\t' && c != '\r') {

                    value += c;
                    c = data.charAt(++i);
                }
            }

            // add the value to the array
            if (array.length <= row) 
                array.push(new Array());

            array[row].push(value);

            // skip whitespaces
            while (c == ' ' || c == '\t' || c == '\r') {
                c = data.charAt(++i);
            }

            // go to the next row or column
            if (c == delimeter) {
                // to the next column
                col++;
            }

            else if (c == newline) {
                // to the next row
                col = 0;
                row++;
            }

            else if (c != eof) {
                // unexpected character
                throw "Delimiter expected after character " + i;
            }

            // go to the next character
            c = data.charAt(++i);
        }  

        return array;
    }

    // expects `array` as an output of csv2array function (2-dim array)
    function extractPoints(array, headingSettings) {

        // Points consists of jsPoint elements
        let points = new Array(), 
            annotations = new Array();
        const colNum = array[0].length;
        const aCol = headingSettings.annotationColumn;
        const xCol = headingSettings.xColumn;
        const yCol = headingSettings.yColumn;

        for (row = 1; row < array.length; row++) {

            if (array[row].length != colNum) {
                console.log(`extractPoints: row ${row} has different from heading column number`);
                return points;
            }

            points.push(new jsPoint(array[row][xCol], array[row][yCol]));
            annotations.push(array[row][aCol]);
        }

        return {ps: points, as: annotations};
    }





    const goButton = document.querySelector("#goButton");
    goButton.addEventListener('click', buildGraphics);

    const plusButton = document.querySelector("#plusButton");
    plusButton.addEventListener('click', scaleUp);

    const minusButton = document.querySelector("#minusButton");
    minusButton.addEventListener('click', scaleDown);

    const canvasH = 300;
    const canvas = document.querySelector("#canvas");
    canvas.style.height = canvasH;

    var gr = new jsGraphics(canvas);
    gr.setCoordinateSystem("cartecian");
    gr.setOrigin(new jsPoint(20, canvasH - 20));
    gr.showGrid();


    // if (canvas.contains())

    

    //Create jsColor object
    var color = new jsColor("red");

    //Create jsPen object
    var pen = new jsPen(color,1);
    const headingSettings = {
        annotationColumn: 0, 
        xColumn: 1, 
        yColumn: 2
    }

    let chartData = extractPoints(csv2array(document.getElementById("data").value), 
            headingSettings);

    function redrawChartData() {
        gr.clear();
        gr.showGrid();
        gr.drawPolyline(pen, chartData.ps);
        chartData.as.forEach((annotation, index) => {
            gr.drawText(annotation, chartData.ps[index]);
        });
    }

    function buildGraphics() {
        chartData = extractPoints(csv2array(document.getElementById("data").value), 
            headingSettings);
        redrawChartData();
    }

    function scaleUp() {
        gr.setScale(gr.getScale() + 0.1);
        redrawChartData();
    }

    function scaleDown() {
        gr.setScale(gr.getScale() - 0.1);
        redrawChartData();
    }
});
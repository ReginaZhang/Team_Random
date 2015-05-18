//***********Constants***********

var POUNDS_IN_STONE = 14;
var KGS_PER_POUND = 0.453592;
var INCHES_IN_FOOT = 12;
var CMS_PER_INCH = 2.54;
var CMS_PER_METRE = 100;
var INPUT_ERROR_MSG = "That was not a valid measurement unit. Please try again";
var IDEAL_BMI_LOWER = 18.5;
var IDEAL_BMI_UPPER = 25;

//***********Global Variables***********

var stoneField = document.forms["bmiForm"]["stone"];
var poundsField = document.forms["bmiForm"]["pounds"]
var kgsField = document.forms["bmiForm"]["kgs"];
var feetField = document.forms["bmiForm"]["feet"];
var inchesField = document.forms["bmiForm"]["inches"];
var cmsField = document.forms["bmiForm"]["cms"];

//***********Register Event handlers***********

document.getElementById("button").onclick = function() { outputBmi() };
stoneField.onchange = function() { updateForm( "stonefld" ) };
stoneField.onchange = function() { updateForm( "poundsfld" ) };
kgsField.onchange = function() { updateForm( "kgsfld" ) };
feetField.onchange = function() { updateForm ( "feetfld" ) };
inchesField.onchange = function() { updateForm ( "inchesfld" ) };
cmsField.onchange = function() { updateForm ( "cmsfld" ) };

//***********Helper rounding number Functions***********

function roundTwoDecimals ( number )
{
    return  Math.round ( number * 100 )  / 100 ;
}

function roundOneDecimal ( number )
{
    return  Math.round ( number * 10 )  / 10 ;
}

function toInteger(number)
{
    return Math.round ( Number ( number ) );
}


function toIntegerFloor ( number )
{
    return Math.floor ( Number ( number) );
}

//***********Helper function for updateForm***********


// deal with a Not a Number input in the specified field
function processNaNInput ( field )
{
    field.value = "";
    field.focus();
    alert(INPUT_ERROR_MSG);
}

//**********************

//  updates the fields for the corresponding units of measure after a field is updated
function updateForm ( updatedField )
{
    if ( updatedField === "stonefld" || updatedField === "poundsfld" )
    {
        if ( isNaN ( stoneField.value ) )
        {
            processNaNInput (stoneField);
        }
        else if ( isNaN ( poundsField.value ) )
        {
            processNaNInput(poundsField);

        }
        else if ( poundsField.value === "" && stoneField.value === "" )
        {
            kgsField.value = "";
        }
        else
        {
            var stoneFldInKgs = ( stoneField.value * POUNDS_IN_STONE ) * KGS_PER_POUND ;
            var poundsFldInKgs = poundsField.value * KGS_PER_POUND;
            var totalKgs = stoneFldInKgs + poundsFldInKgs;
            kgsField.value = roundTwoDecimals ( totalKgs );
        }
    }

    else if ( updatedField === "kgsfld" )
    {
        if ( isNaN ( kgsField.value ) )
        {
            processNaNInput( kgsField );
        }
        else if ( kgsField.value === "")
        {
            stoneField.value = "";
            poundsField.value = "";
        }
        else
        {
            var totalKgsInPounds = kgsField.value / KGS_PER_POUND;
            var numStone = toIntegerFloor ( totalKgsInPounds / POUNDS_IN_STONE );
            var numPounds = roundTwoDecimals ( totalKgsInPounds % POUNDS_IN_STONE );

            stoneField.value =  numStone;
            poundsField.value = numPounds;
        }
    }

    else if ( updatedField === "feetfld" || updatedField === "inchesfld" )
    {
        if ( isNaN ( feetField.value ) )
        {
            processNaNInput(feetField);

        }
        else if ( isNaN ( inchesField.value ) )
        {
            processNaNInput(inchesField);

        }
        else if ( feetField.value === "" && inchesField.value === "" )
        {
            cmsField.value = "";
        }
        else
        {
            var feetFldInCms = ( feetField.value * INCHES_IN_FOOT ) * CMS_PER_INCH;
            var inchesFldInCms = inchesField.value * CMS_PER_INCH;
            var totalCms = feetFldInCms + inchesFldInCms;
            cmsField.value = roundTwoDecimals ( totalCms );
        }
    }

    else if ( updatedField === "cmsfld" )
    {
        if ( isNaN ( cmsField.value ) )
        {
            processNaNInput(cmsField);

        }
        else if ( cmsField.value === "" )
        {
            feetField.value = "";
            inchesField.value = "";
        }
        else
        {
            var cmsFldInInches = cmsField.value / CMS_PER_INCH;
            var numFeet = cmsFldInInches / INCHES_IN_FOOT;
            var numInches = cmsFldInInches % INCHES_IN_FOOT;
            feetField.value = toIntegerFloor ( numFeet );
            inchesField.value = roundOneDecimal ( numInches );
        }
    }

    else
    {
        alert ( "Invalid parmater passed to completeForm function" )
    }

}

//***********Helper Functions Called by calculateBMI***********


// check if any of the fields in the form are empty and if empty outputs
// an error message. Returns false is the form is fully filled out
function hasEmptyFlds ( )
{

    // If all fields are empty output a message
    if ( ( stoneField.value === null || stoneField.value === "" ) && ( poundsField.value === null || poundsField.value === "" ) && ( kgsField.value === null || kgsField.value === "" ) &&
        ( feetField.value === null || feetField.value === "" ) && ( inchesField.value === null || inchesField.value === "" ) && ( cmsField.value === null || cmsField.value === "" ) )
    {
        alert( "Please enter a weight and height" );

        return true;
    }
    // If weight fields of the form are not filled out output an message
    else if (  kgsField.value === null || kgsField.value === "" )
    {
        alert ( "Please enter a weight" );
        return true;
    }
    // If the height fields of the form are not filled out output a message
    else if (  cmsField.value === null || cmsField.value === ""  )
    {
        alert ( "Please enter a height" );
        return true;
    }
    else
    {
        return false;
    }
}

// returns the name of the bmi category for a given bmi
function getBmiCategory ( bmi )
{
    if ( bmi < 15 )
    {
        return "Very severely underweight";
    }
    else if ( bmi < 16 )
    {
        return "Severely underweight";
    }
    else if ( bmi < 18.5 )
    {
        return "Underweight";
    }
    else if ( bmi < 25 )
    {
        return "Normal (healthy weight)";
    }
    else if ( bmi < 30 )
    {
        return "Overweight";
    }
    else if ( bmi < 35 )
    {
        return "Moderately obese";
    }
    else if ( bmi < 40 )
    {
        return "Severely obese";
    }
    else
    {
        return "Very severely obese";
    }
}

//**********************

// calculates and outputs bmi, bmi category, ideal bmi range, ideal weight range and percent over or under ideal weight
function outputBmi ( )
{
    if ( ! hasEmptyFlds ( )  )
    {
        var heightInMetres = cmsField.value / 100;
        var bmi = roundOneDecimal ( kgsField.value /  ( heightInMetres * heightInMetres ) );
        var category = getBmiCategory ( bmi );

        //lower weight cut off for a healthy bmi
        var lowerKgs = toInteger ( IDEAL_BMI_LOWER * ( heightInMetres * heightInMetres ) );
        //upper weight cut off for a healthy bmi
        var upperKgs = toInteger ( IDEAL_BMI_UPPER * ( heightInMetres * heightInMetres ) );

        //convert lower cut off from kgs to stone and pounds
        var totalLowerPounds = lowerKgs / KGS_PER_POUND;
        var lowerStone = toIntegerFloor  ( totalLowerPounds / POUNDS_IN_STONE );
        var lowerPounds = toInteger ( totalLowerPounds % POUNDS_IN_STONE );

        //convert upper cut off from kgs to stone and pounds
        var totalUpperPounds = upperKgs / KGS_PER_POUND;
        var upperStone = toIntegerFloor  ( totalUpperPounds / POUNDS_IN_STONE );
        var upperPounds = toInteger ( totalUpperPounds % POUNDS_IN_STONE );

        var html = "<p id = 'results'>";
        html += "Your BMI is : " + bmi + " <br /> Your BMI is in the " + category + " category<br />";

        html += "<br />The BMI for Normal ( healthy weight ) is between 18.5 and 25";
        html += "<br /><br />Normal (healthy weight) for your height is between:<br />";
        html += lowerKgs + " kgs (" + lowerStone + " stone, " + lowerPounds + " pounds ) and ";
        html += upperKgs + " kgs (" + upperStone + " stone, " + upperPounds + " pounds ) <br />";

        if ( kgsField.value < lowerKgs )
        {
            var kgsUnderIdeal = lowerKgs - kgsField.value;
            var percentUnder = toInteger ( ( kgsUnderIdeal * 100 ) / lowerKgs );
            html += "<br />You are " + percentUnder + "% below your ideal weight bracket";
        }
        else if ( kgsField.value > upperKgs )
        {
            var kgsOverIdeal = kgsField.value - upperKgs;
            var percentOver = toInteger ( ( kgsOverIdeal * 100 ) / upperKgs );
            html += "<br />You are " + percentOver + "% over your ideal weight bracket";
        }

        //html += "<br /><br /><a href='#' >< Go back to bmi form</a></p>"

        document.getElementById( 'bim_result' ).innerHTML = html;
        //showVisualization(bmi);

    }

}

function showVisualization(bmi){
    var graph = '<script>
            var g = new JustGage({
                id: "gauge",
                value: '+ bmi +',
                min: 0,
                max: 100,
                title: "BMI",
                levelColors:["#4EA1E5","#3E97DE"]
            });
            </script>';
    document.getElementById("bmi_visualization").innerHTML = graph;
}

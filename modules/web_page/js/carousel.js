/**
 * Created by liubingfeng on 13/03/15.
 */
function test_image_rotation() {
    var imgObj = document.getElementById("test_image");
    var degree;
    if (imgObj.style.transform == "") {
        console.log(imgObj.style.transform + "hello world");
        degree = 10;
        console.log(imgObj.style.transform = "rotate(" + degree + "deg)");
    }
    else {
        degree = parseInt(imgObj.style.transform.match(/[0-9]+/)[0]);
        if (degree >= 364) {
            degree = 0;
            imgObj.style.transform = "rotate(" + degree + "deg)";
        }
        else {
            degree += 1;
            imgObj.style.transform = "rotate(" + degree + "deg)";
        }
    }
}
function test_image_3d_moving() {
}
//# sourceMappingURL=carousel.js.map
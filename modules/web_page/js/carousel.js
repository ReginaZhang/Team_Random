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
function get_image_position() {
    var obj_collection = document.getElementsByClassName("balloon");
    var i = 0;
    console.log("in function");
    console.log(obj_collection.length);
    for (i; i < obj_collection.length; i++) {
        console.log(obj_collection[i].getAttribute("image-position"));
    }
}
function return_image_position_0(obj_collection) {
    var i, index = -1;
    for (i = 0; i < obj_collection.length; i++) {
        if (parseInt(obj_collection[i].getAttribute("image-position")) == 0) {
            index = i;
            return index;
        }
    }
    return index;
}
function carousel_moving(left_right) {
    var i, index;
    /*current focused item's index, i.e. middle one*/
}
//# sourceMappingURL=carousel.js.map
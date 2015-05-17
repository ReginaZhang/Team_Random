//image-position means image's position on carousel
//middle element image-position =0  ,left =-1 ,right =1
//add click event to all carousel elements
function getImage() {
    var xhr = new XMLHttpRequest();
    var obj_json = null;
    xhr.open("GET", "http://45.56.85.191/food/get_filenames?subdir_name=%2Fstatic%2Fassets&file_type=*");
    xhr.send();
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4 && xhr.status == 200) {
            console.log(xhr.responseText);
            obj_json = JSON.parse(xhr.responseText);
            console.log(obj_json["files"]);
        }
    };
}
function setUpImageTag(obj_json) {
    var domain = "http://45.56.85.191/static/assets/";
    var file_name_array = obj_json["files"];
    var i;
    var image_tag = null;
    var carousel_section = document.getElementById("carousel_section");
    carousel_section.innerHTML = "";
    for (i = 0; i < file_name_array.length && i < 20; i++) {
        image_tag = document.createElement('img');
        image_tag.className = "carousel_element";
        image_tag.src = domain + file_name_array[i];
        carousel_section.appendChild(image_tag);
    }
}
function initialize_carousel_eventListener() {
    var carousel_elements = document.getElementsByClassName("carousel_element");
    var i = 0;
    for (i < 0; i < carousel_elements.length; i++) {
        carousel_elements[i].addEventListener("click", function () {
            jump_to_carousel_element(this);
        });
    }
}
//detecting arrow key pressing
document.onkeydown = function (e) {
    switch (e.keyCode) {
        case 37:
            carousel_moving(1);
            break;
        case 39:
            carousel_moving(-1);
            break;
    }
};
function test_function() {
    console.log("in test func");
}
function jump_to_carousel_element(one_element) {
    var index_one_element = return_current_carousel_image_position(one_element);
    var index_middle_element = return_image_position_0();
    //if jump+times == negative means shift to right
    var jump_times = index_one_element - index_middle_element;
    var i;
    for (i = 0; i < Math.abs(jump_times); i++) {
        //shift left
        if (jump_times > 0) {
            carousel_moving(1);
        }
        else {
            carousel_moving(-1);
        }
    }
    console.log("in jumping func " + "one element index : " + index_one_element + "mid index: " + index_middle_element + " src + " + one_element.src);
}
function get_image_position() {
    var obj_collection = document.getElementsByClassName("carousel_element");
    var i = 0;
    console.log("in function");
    console.log(obj_collection.length);
    for (i; i < obj_collection.length; i++) {
        console.log(obj_collection[i].getAttribute("image-position"));
    }
}
//return clicked carousel element's index position (start from 0 )
function return_current_carousel_image_position(one_element) {
    var carousel_elements = document.getElementsByClassName("carousel_element");
    var i = 0;
    for (i; i < carousel_elements.length; i++) {
        if (carousel_elements[i] == one_element) {
            return i;
        }
    }
    return -1;
}
function return_image_position_0() {
    var i, index = -1;
    var obj_collection = document.getElementsByClassName("carousel_element");
    for (i = 0; i < obj_collection.length; i++) {
        if (parseInt(obj_collection[i].getAttribute("image-position")) == 0) {
            index = i;
            return index;
        }
    }
    return index;
}
function carousel_moving(left_right) {
    var i, index, shift_index, obj_collection;
    obj_collection = document.getElementsByClassName("carousel_element");
    index = return_image_position_0();
    /*current focused item's index, i.e. middle one*/
    shift_index = index + parseInt(left_right);
    var obj_collection_outer_div = document.getElementById("carousel_section");
    var obj_collection_outer_div_width = obj_collection_outer_div.offsetWidth;
    var obj_one_width = obj_collection[0].offsetWidth;
    //if the middle one is clicked
    if (shift_index < 0 || shift_index >= obj_collection.length) {
        return;
    }
    /*-1 move left ..*/
    if (parseInt(left_right) == 1) {
        /*last focused one will be on right*/
        obj_collection[index].setAttribute("image-position", "-1");
        obj_collection[shift_index].setAttribute("image-position", "0");
        set_carousel_transform(obj_collection[index], 50 + 20 * index + "px", "400px", "45deg", "-100px", "" + index);
        set_carousel_transform(obj_collection[shift_index], (obj_collection_outer_div_width / 2) - (obj_one_width / 2) + "px", "400px", "0deg", "25px", shift_index + "");
    }
    else {
        obj_collection[index].setAttribute("image-position", "1");
        obj_collection[shift_index].setAttribute("image-position", "0");
        set_carousel_transform(obj_collection[index], (obj_collection_outer_div_width - obj_one_width) - 50 - 20 * (obj_collection.length - index) + "px", "400px", "-45deg", "-100px", "" + (obj_collection.length - index));
        set_carousel_transform(obj_collection[shift_index], (obj_collection_outer_div_width / 2) - (obj_one_width / 2) + "px", "400px", "0deg", "25px", shift_index + "");
    }
    /*
    console.log(left_right+" index : "+index+" shift index "+shift_index);
    console.log("last focused image-position"+obj_collection[index].getAttribute("image-position"));
    console.log("new/shift focused image-position"+obj_collection[shift_index].getAttribute("image-position"));*/
    console.log(obj_collection[0].getAttribute("image-position") + obj_collection[1].getAttribute("image-position") + obj_collection[2].getAttribute("image-position"));
}
function carousel_initialize() {
    var obj_collection = document.getElementsByClassName("carousel_element");
    var obj_collection_length = obj_collection.length;
    var obj_collection_outer_div = document.getElementById("carousel_section");
    //get carousel div width
    var obj_collection_outer_div_width = obj_collection_outer_div.offsetWidth;
    //get carousel each element's width
    var obj_one_width = obj_collection[0].offsetWidth;
    console.log(document.getElementById("carousel_section").style.width);
    var i = 0;
    //get the element ,set it in the middle as initialization
    var mid_index = Math.floor(obj_collection_length / 2);
    console.log("mid index " + mid_index);
    var one_element;
    for (i = 0; i < mid_index; i++) {
        //all things on left
        one_element = obj_collection[i];
        one_element.setAttribute("image-position", "-1");
        set_carousel_transform(one_element, 50 + 20 * i + "px", "400px", "45deg", "-100px", "" + i);
    }
    //set middle element's attribute
    one_element = obj_collection[mid_index];
    one_element.setAttribute("image-position", "0");
    set_carousel_transform(one_element, (obj_collection_outer_div_width / 2) - (obj_one_width / 2) + "px", "400px", "0deg", "25px", "" + mid_index);
    for (i = mid_index + 1; i < obj_collection_length; i++) {
        one_element = obj_collection[i];
        one_element.setAttribute("image-position", "1");
        set_carousel_transform(one_element, (obj_collection_outer_div_width - obj_one_width) - 50 - 20 * (obj_collection_length - i) + "px", "400px", "-45deg", "-100px", "" + (obj_collection_length - i));
    }
    console.log(obj_collection[0].getAttribute("image-position") + obj_collection[1].getAttribute("image-position") + obj_collection[2].getAttribute("image-position"));
}
//this function will set the position of each element tag on the carousel
//one_element = current carousel is been operated
//left is the position of  carousel element
//perspective is how strong the 3d effect is
//tranlateZ is the position of carousel in z axis
//z_index is the layer of carousel element
function set_carousel_transform(one_element, left, perspective, degree, translateZ, z_index) {
    one_element.style.zIndex = z_index;
    one_element.style.left = left;
    console.log("left:" + left);
    one_element.style.webkitTransform = "perspective(" + perspective + ") rotateY(" + degree + ") translateZ(" + translateZ + ")";
    one_element.style.MozTransform = "perspective(" + perspective + ") rotateY(" + degree + ") translateZ(" + translateZ + ")";
    one_element.style.transform = "perspective(" + perspective + ") rotateY(" + degree + ") translateZ(" + translateZ + ")";
    //one_element.style.MozTransform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
    //one_element.style.transform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
}
//getImage() 
//# sourceMappingURL=carousel.js.map
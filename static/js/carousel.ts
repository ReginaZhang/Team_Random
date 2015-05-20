/*
 * This file is created by Team_Random for Health Overflow Web App Project
 * Jonathan Barnard: 677638
 * Zhanhuai Huang: 634154
 * Nan Lin: 651025
 * Ruijing Zhang: 646179
 * Bingfeng Liu: 639187
 * */

//image-position means image's position on carousel
//middle element image-position =0  ,left =-1 ,right =1
//add click event to all carousel elements
//this function will load image url from our vps folder
//after setting up all image tag , they will be used for setting up carousel
function getImage()
{
    var xhr = new XMLHttpRequest();
    var obj_json=null;
    //this api will give back the image file names on our vps ,and each time the order of file name is shuffled.
    xhr.open("GET","http://45.56.85.191/food/get_random_filenames?subdir_name=%2Fstatic%2Fassets%2Fcarousel_image&file_type=*");
    xhr.send();
    xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status==200) {
            console.log(xhr.responseText);
            obj_json=JSON.parse(xhr.responseText);
            console.log(obj_json["files"]);
            //add img tag based on image file name
            setUpImageTag(obj_json);
            //set up the initial position of img in carousel
            carousel_initialize();
            //add onclick listener to all carousel img tag
            initialize_carousel_eventListener();
        }
    }
}

//this function will take the image file names and parse them into img tag
function setUpImageTag(obj_json)
{
    var domain="http://45.56.85.191/static/assets/carousel_image/";
    var file_name_array=obj_json["files"];
    var i;
    var image_tag =null;
    var carousel_section=document.getElementById("carousel_section");
    carousel_section.innerHTML="";
    for(i=0;i<file_name_array.length&&i<20;i++)
    {
        image_tag = document.createElement('img');
        image_tag.className="carousel_element";
        image_tag.src=domain+file_name_array[i];
        //console.log(file_name_array[i]);
        carousel_section.appendChild(image_tag);
    }

}



//add onclick event listener to all img tag in carousel
//so the img on carousel can be click to move position
function initialize_carousel_eventListener()
{
    //al img tag will have class name == carousel_element
    var carousel_elements=document.getElementsByClassName("carousel_element");
    var i =0;
    for(i<0;i<carousel_elements.length;i++)
    {
        carousel_elements[i].addEventListener("click",function(){jump_to_carousel_element(this);});
    }

}

//detecting arrow key pressing
document.onkeydown=function(e)
{
    switch (e.keyCode)
    {
        //left arrow key,middle picture move left
        case 37:
            carousel_moving(1);
            break;
        //right arrow key,middle picture move right
        case 39:
            carousel_moving(-1);
            break;
    }
}

//this function is used to jump to specific picture clicked on carousel
function jump_to_carousel_element(one_element):void
{
    //get the position of current pictures in the carousel.
    var index_one_element:number=return_current_carousel_image_position(one_element);
    // for middle image ,its 'image_position' value ==0 , we need to get its position
    var index_middle_element:number=return_image_position_0();
    //if jump+times == negative means shift to right
    var jump_times:number=index_one_element-index_middle_element;
    var i:number;
    //apply move to left or right n times based on the position of the clicked image with respect to the middle image.
    for(i=0;i<Math.abs(jump_times);i++)
    {
        //shift left
        if(jump_times>0)
        {
            carousel_moving(1)
        }
        //shift right
        else
        {
            carousel_moving(-1)
        }
    }
    //console.log("in jumping func "+"one element index : "+index_one_element+"mid index: "+index_middle_element+" src + "+one_element.src);
}


/*
function get_image_position():void
{
    var obj_collection=document.getElementsByClassName("carousel_element");
    var i=0;
    //console.log("in function");
    //console.log(obj_collection.length);
    for (i ;i<obj_collection.length;i++)
    {
        //console.log((<HTMLElement>obj_collection[i]).getAttribute("image-position"));
    }
}
*/

//return clicked carousel element's index position (start from 0 )
function return_current_carousel_image_position(one_element):number
{
    var carousel_elements=document.getElementsByClassName("carousel_element");
    var i=0;
    for(i;i<carousel_elements.length;i++)
    {
        if(carousel_elements[i]==one_element)
        {
            return i;
        }
    }
    return -1;
}
//find the position of middle image in the carousel .
function return_image_position_0():number
{
    var i:number,index:number=-1;
    var obj_collection=document.getElementsByClassName("carousel_element");
    //middle img will have image-position ==0, all the image on its left will have image-position==-1,and right will be 1
    for(i=0;i<obj_collection.length;i++)
    {
        if(parseInt((<HTMLElement>obj_collection[i]).getAttribute("image-position"))==0)
        {
            index=i;
            return index;
        }
    }
    return index
}
//this will move the middle image to left or right and move the corresponding image to the right position
function carousel_moving(left_right):void
{
    var left_part_offset=70;
    var right_part_offset=50;
    var image_offest=20;
    var i:number,index:number,shift_index:number,obj_collection;
    obj_collection=document.getElementsByClassName("carousel_element");
    //get middle image of carousel 's position among all the images.
    index=return_image_position_0();
    /*shift_index will be the position of the image on the middle image's right or left it will be shift to middle*/
    shift_index=index+parseInt(left_right);
    var obj_collection_outer_div=document.getElementById("carousel_section");
    //get carousel div width
    var obj_collection_outer_div_width=obj_collection_outer_div.offsetWidth;
    //get img width
    var obj_one_width=(<HTMLElement>obj_collection[0]).offsetWidth;

    //if the middle one is clicked
    if(shift_index<0||shift_index>=obj_collection.length)
    {
        return;
    }
    //shift left
    if(parseInt(left_right)==1)
    {
        /*last focused(middle ) one will be on left so set its image-position to -1*/
        obj_collection[index].setAttribute("image-position","-1");
        //set new middle image's image-position to 0
        obj_collection[shift_index].setAttribute("image-position","0");
        //push the 'last middle' image to left .
        set_carousel_transform(obj_collection[index],left_part_offset+image_offest*index+"px","400px","45deg","-100px",""+index);
        //new middle image on carousel will be in the middle of carousel div
        set_carousel_transform(obj_collection[shift_index],(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px","50");
    }
    //shift right
    else
    {
        //most of the operations below are the opposite operation to the above
        obj_collection[index].setAttribute("image-position","1");
        obj_collection[shift_index].setAttribute("image-position","0");
        set_carousel_transform(obj_collection[index],(obj_collection_outer_div_width-obj_one_width)-right_part_offset-image_offest*(obj_collection.length-index-1)+"px","400px","-45deg","-100px",""+(obj_collection.length-index));

        set_carousel_transform(obj_collection[shift_index],(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px","50");
    }
    /*
    console.log(left_right+" index : "+index+" shift index "+shift_index);
    console.log("last focused image-position"+obj_collection[index].getAttribute("image-position"));
    console.log("new/shift focused image-position"+obj_collection[shift_index].getAttribute("image-position"));*/

    console.log(obj_collection[0].getAttribute("image-position") +obj_collection[1].getAttribute("image-position")+obj_collection[2].getAttribute("image-position"));
}


//this function is to set up images position in carousel in the start
function carousel_initialize():void
{
    var left_part_offset=50;
    var right_part_offset=50;
    var image_offset=20;
    var obj_collection=document.getElementsByClassName("carousel_element");
    //number of image will be in carousel
    var obj_collection_length=obj_collection.length;
    var obj_collection_outer_div=document.getElementById("carousel_section");
    //get carousel div width
    var obj_collection_outer_div_width=obj_collection_outer_div.offsetWidth;
    //get carousel each element's width
    var obj_one_width=(<HTMLElement>obj_collection[0]).offsetWidth;

    //console.log(document.getElementById("carousel_section").style.width);
    var i=0;
    //get the element ,set it in the middle as initialization
    var mid_index=Math.floor(obj_collection_length/2);
    //console.log("mid index "+mid_index);
    var one_element:HTMLElement;
    //elements == images in carousel
    //set all left part element's attribute
    for(i=0;i<mid_index;i++)
    {
        //all things on left
        one_element=<HTMLElement>obj_collection[i];
        //image on the left of middle image of carousel will have image-position = -1
        one_element.setAttribute("image-position","-1");
        //left_part_offset+image_offest*i == > most left image will start from left_part_offest, and
        //all image after it will have a gap of image_offest
        set_carousel_transform(one_element,left_part_offset+image_offset*i+"px","400px","45deg","-100px",""+i);
    }
    //set middle element's attribute
    one_element=<HTMLElement>obj_collection[mid_index];
    one_element.setAttribute("image-position","0");
    //(obj_collection_outer_div_width/2)-(obj_one_width/2) ==> initially middle image of carousel will be in the middle of div
    set_carousel_transform(one_element,(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px",""+mid_index);
    //set all right part element's attribute
    for(i=mid_index+1;i<obj_collection_length;i++)
    {
        one_element=<HTMLElement>obj_collection[i];
        one_element.setAttribute("image-position","1");
        //obj_collection_outer_div_width-obj_one_width)-right_part_offset-image_offset*(obj_collection_length-i)==> first right image will be right_part_offset pixel from most right of carousel div
        //all left part image will have image_offset pixel gap from most right towards inner middle picture.
        set_carousel_transform(one_element,(obj_collection_outer_div_width-obj_one_width)-right_part_offset-image_offset*(obj_collection_length-i-1)+"px","400px","-45deg","-100px",""+(obj_collection_length-i));
    }
    //console.log((<HTMLElement>obj_collection[0]).getAttribute("image-position") +(<HTMLElement>obj_collection[1]).getAttribute("image-position")+(<HTMLElement>obj_collection[2]).getAttribute("image-position"));

}

//this function will set the position of each element tag on the carousel
//one_element = current carousel is been operated
//left is the position of  carousel element
//perspective is how strong the 3d effect is
//tranlateZ is the position of carousel in z axis
//z_index is the layer of carousel element
function set_carousel_transform(one_element,left:string,perspective:string,degree:string,translateZ:string,z_index:string):void
{
    one_element.style.zIndex=z_index;
    one_element.style.left=left;
    console.log("left:"+left );
    one_element.style.webkitTransform="perspective("+perspective+") rotateY("+degree+") translateZ("+translateZ+")";
    one_element.style.MozTransform="perspective("+perspective+") rotateY("+degree+") translateZ("+translateZ+")";
    one_element.style.transform="perspective("+perspective+") rotateY("+degree+") translateZ("+translateZ+")";
    //one_element.style.MozTransform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
    //one_element.style.transform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
}


getImage();


//carousel_initialize();
//initialize_carousel_eventListener();
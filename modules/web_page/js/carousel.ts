/**
 * Created by liubingfeng on 13/03/15.
 */
function test_image_rotation():void
{
    var imgObj:HTMLImageElement=<HTMLImageElement>document.getElementById("test_image");
    var degree:number;
    if(imgObj.style.transform=="")
    {
        console.log(imgObj.style.transform+"hello world");
        degree=10;
        console.log(imgObj.style.transform = "rotate("+degree+"deg)");

    }
    else
    {
        degree=parseInt(imgObj.style.transform.match(/[0-9]+/)[0]);
        if (degree>=364)
        {
            degree=0;
            imgObj.style.transform = "rotate("+degree+"deg)";
        }
        else
        {
            degree+=1;
            imgObj.style.transform = "rotate(" + degree + "deg)";
        }
        //console.log("in function");
        //console.log(imgObj.style.transform);
        //console.log(imgObj.style.transform.match(/[0-9]+/));
    }

}

function test_image_3d_moving():void
{

}

function get_image_position():void
{
    var obj_collection=document.getElementsByClassName("carousel_element");
    var i=0;
    console.log("in function");
    console.log(obj_collection.length);
    for (i ;i<obj_collection.length;i++)
    {
        console.log((<HTMLElement>obj_collection[i]).getAttribute("image-position"));
    }
}

function return_image_position_0(obj_collection):number
{
    var i:number,index:number=-1;
    for(i=0;i<obj_collection.length;i++)
    {
        if(parseInt(obj_collection[i].getAttribute("image-position"))==0)
        {
            index=i;
            return index;
        }
    }
    return index
}

function carousel_moving(left_right):void
{
    var i:number,index:number,shift_index:number,obj_collection;
    obj_collection=document.getElementsByClassName("carousel_element");
    index=return_image_position_0(obj_collection);
    /*current focused item's index, i.e. middle one*/
    shift_index=index+parseInt(left_right);
    var obj_collection_outer_div=document.getElementById("carousel_section");
    var obj_collection_outer_div_width=obj_collection_outer_div.offsetWidth;
    var obj_one_width=(<HTMLElement>obj_collection[0]).offsetWidth;


    if(shift_index<0||shift_index>=obj_collection.length)
    {
        return;
    }
    /*-1 move left ..*/
    if(parseInt(left_right)==1)
    {
        /*last focused one will be on right*/
        obj_collection[index].setAttribute("image-position","-1");
        obj_collection[shift_index].setAttribute("image-position","0");
        /*
        obj_collection[index].style.webkitTransform="perspective( 400px ) rotateY(-45deg) translateZ( -50px )";
        obj_collection[index].style.MozTransform="perspective( 400px ) rotateY(-45deg) translateZ( -50px )";
        obj_collection[index].style.transform="perspective( 400px ) rotateY(-45deg) translateZ( -50px )";*/
        /*
        obj_collection[shift_index].style.MozTransform="perspective( 400px ) translateZ( 50px )";
        obj_collection[shift_index].style.Transform="perspective( 400px ) translateZ( 50px )";
        obj_collection[shift_index].style.transform="perspective( 400px ) translateZ( 50px )";
         */
        /*
        set_carousel_transform(obj_collection[index],(obj_collection_outer_div_width-obj_one_width)-50-10*(obj_collection.length-index)+"px","400px","-45deg","-50px",""+obj_collection.length-index);

        set_carousel_transform(obj_collection[shift_index],(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px",shift_index+"");*/
        set_carousel_transform(obj_collection[index],50+10*index+"px","400px","45deg","-100px",""+index);
        set_carousel_transform(obj_collection[shift_index],(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px",shift_index+"");
    }
    else
    {
        obj_collection[index].setAttribute("image-position","1");
        obj_collection[shift_index].setAttribute("image-position","0");
        /*obj_collection[index].style.webkitTransform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
        obj_collection[index].style.MozTransform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
        obj_collection[index].style.transform="perspective( 400px ) rotateY(45deg) translateZ( -50px )";
        obj_collection[shift_index].style.MozTransform="perspective( 400px ) translateZ( 50px )";
        obj_collection[shift_index].style.Transform="perspective( 400px ) translateZ( 50px )";
        obj_collection[shift_index].style.transform="perspective( 400px ) translateZ( 50px )";*/
        /*
        set_carousel_transform(obj_collection[index],50+10*(obj_collection.length-index)+"px","400px","45deg","-50px",""+index);

        set_carousel_transform(obj_collection[shift_index],(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px",shift_index+"");*/
        set_carousel_transform(obj_collection[index],(obj_collection_outer_div_width-obj_one_width)-50-10*(obj_collection.length-index)+"px","400px","-45deg","-100px",""+(obj_collection.length-index));

        set_carousel_transform(obj_collection[shift_index],(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px",shift_index+"");
    }
    /*
    console.log(left_right+" index : "+index+" shift index "+shift_index);
    console.log("last focused image-position"+obj_collection[index].getAttribute("image-position"));
    console.log("new/shift focused image-position"+obj_collection[shift_index].getAttribute("image-position"));*/

    console.log(obj_collection[0].getAttribute("image-position") +obj_collection[1].getAttribute("image-position")+obj_collection[2].getAttribute("image-position"));
}

function carousel_initialize():void
{
    var obj_collection=document.getElementsByClassName("carousel_element");
    var obj_collection_length=obj_collection.length;
    var obj_collection_outer_div=document.getElementById("carousel_section");
    var obj_collection_outer_div_width=obj_collection_outer_div.offsetWidth;
    var obj_one_width=(<HTMLElement>obj_collection[0]).offsetWidth;

    console.log(document.getElementById("carousel_section").style.width);
    var i=0;
    var mid_index=Math.floor(obj_collection_length/2);
    console.log("mid index "+mid_index);
    var one_element:HTMLElement;
    for(i=0;i<mid_index;i++)
    {
        //all things on left
        one_element=<HTMLElement>obj_collection[i];
        one_element.setAttribute("image-position","-1");
        set_carousel_transform(one_element,50+10*i+"px","400px","45deg","-100px",""+i);
    }
    one_element=<HTMLElement>obj_collection[mid_index];
    one_element.setAttribute("image-position","0");
    set_carousel_transform(one_element,(obj_collection_outer_div_width/2)-(obj_one_width/2)+"px","400px","0deg","25px",""+mid_index);
    for(i=mid_index+1;i<obj_collection_length;i++)
    {
        one_element=<HTMLElement>obj_collection[i];
        one_element.setAttribute("image-position","1");
        set_carousel_transform(one_element,(obj_collection_outer_div_width-obj_one_width)-50-10*(obj_collection_length-i)+"px","400px","-45deg","-100px",""+(obj_collection_length-i));
    }
    console.log(obj_collection[0].getAttribute("image-position") +obj_collection[1].getAttribute("image-position")+obj_collection[2].getAttribute("image-position"));

}

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

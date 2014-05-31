function Resize(){
	var content = document.getElementById("content");
	if(navigator.userAgent.match(/android|ipad|iphone|ipod/i) != null{
		content.style.width = "80%";
	} else {
		content.style.width = "50%";
	}
}

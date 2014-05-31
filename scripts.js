function Resize(){
	var content = document.getElementById("content");
	if(navigator.userAgent.match(/Android|iPad|iPhone|iPod|Windows Phone/i) != null){
		content.style.width = "80%";
	} else {
		content.style.width = "50%";
	}
}

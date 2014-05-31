function Resize(){
	var content = document.getElementById("content");
	if(navigator.userAgent.match(/Android|iPad|iPhone|iPod|Windows Phone/i) != null || window.innerWidth <= 1380){
		content.style.width = "80%";
	} else {
		content.style.width = "50%";
	}
}

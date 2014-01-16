
function update(dataStr){
    var data = JSON.parse(dataStr)

    if(data && data.update.id){
        var id = data.update.id
        var guest = data.update.guest
        var home= data.update.home

        var homeId = "#home"+id
        var guestId = "#guest"+id
        $(homeId).fadeOut(400,function(){
            $(homeId).text(home).fadeIn()
        })
        $(guestId).fadeOut(400,function(){
            $(guestId).text(guest).fadeIn()
        })
    }

}

var ws = null

$(document).ready(function(){
    ws = new WebSocket("ws://localhost:9000/updates")

    ws.onmessage = function (e) {
        console.log('Server: ' + e.data);
        update(e.data)
    };

})
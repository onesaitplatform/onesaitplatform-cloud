var me = {};
me.avatar = "images/contact.png";

var you = {};
you.avatar = "images/bot.jpg";

function formatAMPM(date) {
    var hours = date.getHours();
    var minutes = date.getMinutes();
    var ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'
    minutes = minutes < 10 ? '0'+minutes : minutes;
    var strTime = hours + ':' + minutes + ' ' + ampm;
    return strTime;
}            

//-- No use time. It is a javaScript effect.
function insertChat(who, text, time){
	console.log('inserting message...');
    if (time === undefined){
        time = 0;
    }
    var control = "";
    var date = formatAMPM(new Date());
    
    if (who == "me"){
        control = '<li style="width:100%; margin: 10px;">' +
                        '<div class="msj macro">' +
                        '<div class="avatar"><img class="img-circle" style="width:100%; opacity: 0.4;" src="'+ me.avatar +'" /></div>' +
                            '<div class="text text-l">' +
                                '<p>'+ text +'</p>' +
                                '<p><small>'+date+'</small></p>' +
                            '</div>' +
                        '</div>' +
                    '</li>';                    
    }else{
        control = '<li style="width:100%; margin: 10px auto;">' +
                        '<div class="msj-rta macro">' +
                            '<div class="text text-r">' +
                                '<p>'+text+'</p>' +
                                '<p><small>'+date+'</small></p>' +
                            '</div>' +
                        '<div class="avatar" style="padding:0px 0px 0px 10px !important"><img class="img-circle" style="width:100%;" src="'+you.avatar+'" /></div>' +                                
                  '</li>';
    }
    setTimeout(
        function(){                        
        $("ul.chatbot").append(control).scrollTop($("ul.chatbot").prop('scrollHeight'));
        }, time);
    
}

function resetChat(){
    $("ul").empty();
}

function buttonPressed(msg){
	console.log('button pressed Order: ' + msg);
    insertChat("me",msg);
    sendMsg(msg); 
}

function sendMsg(text){
    var url = 'message?msg=' + text
    $.get(url, 
        function(returnedData){
            console.log(returnedData);
            var data = JSON.parse(returnedData);
            $(".bot-orders").empty();
            data.buttons.forEach(function(element) {
                var button = '<button class="pressed" onclick="buttonPressed(\''+element+'\')">'+element+'</button>';
                $(".bot-orders").append(button);
            });
            insertChat("you",data.msg);
        });    
}


$(".mytext").on("keydown", function(e){
    if (e.which == 13){
        var text = $(this).val();
        if (text !== ""){
            insertChat("me", text);  
            sendMsg(text);        
            $(this).val('');
        }
    }
});

$('#sendChat').click(function(){
    $(".mytext").trigger({type: 'keydown', which: 13, keyCode: 13});
})

//-- Clear Chat
resetChat();
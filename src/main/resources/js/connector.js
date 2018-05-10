var SERVER_BASE_URL = 'http://localhost:2990/jira';

function verifyTeam() {
    var slack = $('#slack-input');

    if (slack.val().trim()) {
        $.ajax({
            url: SERVER_BASE_URL + '/rest/standbot/latest/slack/verify?subdomain=' + slack.val(),
            //data: { signature: authHeader },
            type: "GET",
            beforeSend: function (xhr) {
                xhr.setRequestHeader('Accept-Language', 'en-US,en;q=0.5');
            },
            success: function (data) {
                console.log(data);
            }
        });
    }
}

const functions = require('firebase-functions');

// replaces keywords with emoji in the "text" key of messages
// pushed to /messages
exports.emojify =
    functions.database.ref('/messages/{pushId}/text')
    .onWrite((change, context) => {
        // Database write events include new, modified, or deleted
        // database nodes. All three types of events at the specific
        // database path trigger this cloud function.
        // For this function we only want to emojify new database nodes,
        // so we'll first check to exit out of the function early if
        // this isn't a new message.

        // !change.after.val() is a deleted event (was !event.data.val())
        // change.before.val() is a modified event (was event.data.previous.val())
        if (!change.after.val() || change.before.val()) {
            console.log("not a new write event");
            return;
        }

        // Now we begin the emoji transformation
        console.log("emojifying!");

        // Get the value from the 'text' key of the message
        const originalText = change.after.val();
        const emojifiedText = emojifyText(originalText);

        // Return a JavaScript Promise to update the database node (change.after instead of event.data)
        return change.after.ref.set(emojifiedText);
    });

// Returns text with keywords replaced by emoji
// Replacing with the regular expression /.../ig does a case-insensitive
// search (i flag) for all occurrences (g flag) in the string
function emojifyText(text) {
    var emojifiedText = text;
    emojifiedText = emojifiedText.replace(/\blol\b/ig, "😂");
    emojifiedText = emojifiedText.replace(/\bcat\b/ig, "😸");
    return emojifiedText;
}

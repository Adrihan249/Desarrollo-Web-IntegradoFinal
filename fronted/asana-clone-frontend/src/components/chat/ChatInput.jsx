// src/components/chat/ChatInput.jsx
import { useState, useRef } from 'react';
import { Send, Smile, Paperclip, AtSign } from 'lucide-react';
import Button from '../common/Button';

const ChatInput = ({ projectId, onSend, replyTo = null, onCancelReply }) => {
  const [message, setMessage] = useState('');
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const inputRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!message.trim()) return;

    onSend({
      content: message.trim(),
      parentMessageId: replyTo?.id || null
    });

    setMessage('');
    inputRef.current?.focus();
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const insertEmoji = (emoji) => {
    setMessage(message + emoji);
    setShowEmojiPicker(false);
    inputRef.current?.focus();
  };

  const commonEmojis = ['😀', '😂', '❤️', '👍', '👏', '🎉', '🔥', '✅'];

  return (
    <div className="border-t p-4">
      {replyTo && (
        <div className="mb-2 p-2 bg-gray-50 rounded-lg flex items-center justify-between">
          <div className="text-sm">
            <span className="text-gray-500">Respondiendo a </span>
            <span className="font-medium">{replyTo.author.firstName}</span>
          </div>
          <button
            onClick={onCancelReply}
            className="text-gray-400 hover:text-gray-600"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex items-end gap-2">
        <div className="flex-1 relative">
          <textarea
            ref={inputRef}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Escribe un mensaje..."
            rows={1}
            className="w-full px-3 py-2 pr-20 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            style={{ minHeight: '40px', maxHeight: '120px' }}
          />

          <div className="absolute right-2 bottom-2 flex items-center gap-1">
            <div className="relative">
              <button
                type="button"
                onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                className="p-1 text-gray-400 hover:text-gray-600 rounded"
              >
                <Smile className="w-5 h-5" />
              </button>

              {showEmojiPicker && (
                <div className="absolute bottom-full right-0 mb-2 bg-white rounded-lg shadow-lg border p-2">
                  <div className="grid grid-cols-4 gap-1">
                    {commonEmojis.map(emoji => (
                      <button
                        key={emoji}
                        type="button"
                        onClick={() => insertEmoji(emoji)}
                        className="text-2xl hover:bg-gray-100 rounded p-1"
                      >
                        {emoji}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>

            <button
              type="button"
              className="p-1 text-gray-400 hover:text-gray-600 rounded"
            >
              <AtSign className="w-5 h-5" />
            </button>
          </div>
        </div>

        <Button
          type="submit"
          variant="primary"
          size="sm"
          disabled={!message.trim()}
        >
          <Send className="w-4 h-4" />
        </Button>
      </form>
    </div>
  );
};

export default ChatInput;
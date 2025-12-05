// src/components/chat/ChatBox.jsx
import { useState, useEffect, useRef } from 'react';
import { Send, Smile, Paperclip, X, MessageSquare } from 'lucide-react';
import ChatMessage from './ChatMessage';
import ChatInput from './ChatInput';
import EmptyState from '../common/EmptyState';
import Spinner from '../common/Spinner';
import chatService from '../../services/chatService';

const ChatBox = ({ projectId, isOpen, onClose }) => {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      loadMessages();
      // Mark messages as read
      markMessagesAsRead();
    }
  }, [projectId, isOpen]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const loadMessages = async () => {
    try {
      const data = await chatService.getMessages(projectId);
      setMessages(data);
    } catch (error) {
      console.error('Error loading messages:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const markMessagesAsRead = async () => {
    try {
      const unreadMessages = messages.filter(m => !m.read);
      for (const message of unreadMessages) {
        await chatService.markAsRead(projectId, message.id);
      }
    } catch (error) {
      console.error('Error marking messages as read:', error);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSendMessage = async (messageData) => {
    try {
      const newMessage = await chatService.sendMessage(projectId, messageData);
      setMessages([...messages, newMessage]);
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  const handleReaction = async (messageId, emoji) => {
    try {
      await chatService.addReaction(projectId, messageId, { emoji });
      await loadMessages();
    } catch (error) {
      console.error('Error adding reaction:', error);
    }
  };

  const handleDeleteMessage = async (messageId) => {
    if (!window.confirm('¿Estás seguro de eliminar este mensaje?')) return;

    try {
      await chatService.deleteMessage(projectId, messageId);
      setMessages(messages.filter(m => m.id !== messageId));
    } catch (error) {
      console.error('Error deleting message:', error);
    }
  };

  const handlePinMessage = async (messageId) => {
    try {
      await chatService.pinMessage(projectId, messageId);
      await loadMessages();
    } catch (error) {
      console.error('Error pinning message:', error);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed bottom-4 right-4 w-96 h-[600px] bg-white rounded-lg shadow-2xl flex flex-col z-50">
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b">
        <div className="flex items-center gap-2">
          <MessageSquare className="w-5 h-5 text-indigo-600" />
          <h3 className="font-semibold text-gray-900">Chat del Proyecto</h3>
        </div>
        <button
          onClick={onClose}
          className="text-gray-400 hover:text-gray-600"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {isLoading ? (
          <div className="flex justify-center items-center h-full">
            <Spinner />
          </div>
        ) : messages.length === 0 ? (
          <EmptyState
            icon={<MessageSquare className="w-12 h-12" />}
            title="No hay mensajes"
            description="Inicia la conversación"
          />
        ) : (
          <>
            {messages.map(message => (
              <ChatMessage
                key={message.id}
                message={message}
                onReaction={handleReaction}
                onDelete={handleDeleteMessage}
                onPin={handlePinMessage}
              />
            ))}
            <div ref={messagesEndRef} />
          </>
        )}
      </div>

      {/* Input */}
      <ChatInput
        projectId={projectId}
        onSend={handleSendMessage}
      />
    </div>
  );
};

export default ChatBox;
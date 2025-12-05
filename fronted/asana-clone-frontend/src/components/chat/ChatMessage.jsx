// src/components/chat/ChatMessage.jsx
import { useState } from 'react';
import { MoreVertical, Reply, Pin, Trash2, Edit2 } from 'lucide-react';
import Avatar from '../common/Avatar';
import MessageReactions from './MessageReactions';
import { formatDate } from '../../utils/formatters';
import { useAuth } from '../../hooks/useAuth';

const ChatMessage = ({ message, onReaction, onDelete, onPin, onReply, onEdit }) => {
  const { user } = useAuth();
  const [showActions, setShowActions] = useState(false);

  const isAuthor = user?.id === message.author.id;
  const isOwnMessage = user?.id === message.author.id;

  return (
    <div className={`flex gap-3 group ${isOwnMessage ? 'flex-row-reverse' : ''}`}>
      <Avatar user={message.author} size="sm" />

      <div className={`flex-1 ${isOwnMessage ? 'flex flex-col items-end' : ''}`}>
        <div className="flex items-center gap-2 mb-1">
          <span className="text-sm font-medium text-gray-900">
            {message.author.firstName} {message.author.lastName}
          </span>
          <span className="text-xs text-gray-500">
            {formatDate(message.createdAt)}
          </span>
          {message.pinned && (
            <Pin className="w-3 h-3 text-indigo-600" />
          )}
        </div>

        <div className={`relative max-w-md ${isOwnMessage ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-900'} rounded-lg px-4 py-2`}>
          {message.parentMessage && (
            <div className="mb-2 pb-2 border-b border-opacity-20 text-sm opacity-75">
              <div className="flex items-center gap-1">
                <Reply className="w-3 h-3" />
                <span>Respondiendo a {message.parentMessage.author.firstName}</span>
              </div>
              <p className="truncate">{message.parentMessage.content}</p>
            </div>
          )}

          <p className="whitespace-pre-wrap break-words">{message.content}</p>

          {message.edited && (
            <span className="text-xs opacity-75 mt-1 block">(editado)</span>
          )}

          {/* Actions */}
          <div className={`absolute ${isOwnMessage ? 'left-0 -translate-x-full' : 'right-0 translate-x-full'} top-0 opacity-0 group-hover:opacity-100 transition-opacity`}>
            <div className="relative">
              <button
                onClick={() => setShowActions(!showActions)}
                className="p-1 hover:bg-gray-200 rounded"
              >
                <MoreVertical className="w-4 h-4 text-gray-500" />
              </button>

              {showActions && (
                <div className="absolute top-full mt-1 bg-white rounded-lg shadow-lg border py-1 z-10 min-w-[140px]">
                  <button
                    onClick={() => {
                      onReply(message);
                      setShowActions(false);
                    }}
                    className="w-full px-3 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                  >
                    <Reply className="w-4 h-4" />
                    Responder
                  </button>

                  {isAuthor && (
                    <>
                      <button
                        onClick={() => {
                          onEdit(message);
                          setShowActions(false);
                        }}
                        className="w-full px-3 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                      >
                        <Edit2 className="w-4 h-4" />
                        Editar
                      </button>
                      <button
                        onClick={() => {
                          onDelete(message.id);
                          setShowActions(false);
                        }}
                        className="w-full px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                      >
                        <Trash2 className="w-4 h-4" />
                        Eliminar
                      </button>
                    </>
                  )}

                  <button
                    onClick={() => {
                      onPin(message.id);
                      setShowActions(false);
                    }}
                    className="w-full px-3 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                  >
                    <Pin className="w-4 h-4" />
                    {message.pinned ? 'Desfijar' : 'Fijar'}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Reactions */}
        {message.reactions && message.reactions.length > 0 && (
          <MessageReactions
            reactions={message.reactions}
            onReact={(emoji) => onReaction(message.id, emoji)}
          />
        )}
      </div>
    </div>
  );
};

export default ChatMessage;
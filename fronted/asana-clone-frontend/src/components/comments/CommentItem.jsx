// src/components/comments/CommentItem.jsx
import { useState } from 'react';
import { MessageSquare, Edit2, Trash2, MoreVertical } from 'lucide-react';
import Avatar from '../common/Avatar';
import Button from '../common/Button';
import { formatDate } from '../../utils/formatters';
import { useAuth } from '../../hooks/useAuth';

const CommentItem = ({ 
  comment, 
  onReply, 
  onEdit, 
  onDelete, 
  replies = [],
  level = 0 
}) => {
  const { user } = useAuth();
  const [showActions, setShowActions] = useState(false);
  const [showReplies, setShowReplies] = useState(true);

  const isAuthor = user?.id === comment.author.id;
  const hasReplies = replies.length > 0;

  return (
    <div className={`${level > 0 ? 'ml-12 mt-3' : ''}`}>
      <div className="flex gap-3 group">
        <Avatar user={comment.author} size="sm" />
        
        <div className="flex-1 min-w-0">
          <div className="bg-gray-50 rounded-lg px-4 py-3">
            <div className="flex items-start justify-between mb-2">
              <div>
                <span className="font-medium text-gray-900">
                  {comment.author.firstName} {comment.author.lastName}
                </span>
                <span className="text-sm text-gray-500 ml-2">
                  {formatDate(comment.createdAt)}
                  {comment.edited && ' (editado)'}
                </span>
              </div>

              {isAuthor && (
                <div className="relative">
                  <button
                    onClick={() => setShowActions(!showActions)}
                    className="opacity-0 group-hover:opacity-100 transition-opacity p-1 hover:bg-gray-200 rounded"
                  >
                    <MoreVertical className="w-4 h-4 text-gray-500" />
                  </button>
                  
                  {showActions && (
                    <div className="absolute right-0 mt-1 w-32 bg-white rounded-lg shadow-lg border py-1 z-10">
                      <button
                        onClick={() => {
                          onEdit(comment);
                          setShowActions(false);
                        }}
                        className="w-full px-3 py-2 text-left text-sm hover:bg-gray-50 flex items-center gap-2"
                      >
                        <Edit2 className="w-3 h-3" />
                        Editar
                      </button>
                      <button
                        onClick={() => {
                          onDelete(comment.id);
                          setShowActions(false);
                        }}
                        className="w-full px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2"
                      >
                        <Trash2 className="w-3 h-3" />
                        Eliminar
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>

            <p className="text-gray-700 whitespace-pre-wrap">{comment.content}</p>
          </div>

          {/* Actions */}
          <div className="flex items-center gap-4 mt-2 ml-4">
            <button
              onClick={() => onReply(comment)}
              className="text-sm text-gray-500 hover:text-indigo-600 flex items-center gap-1"
            >
              <MessageSquare className="w-3 h-3" />
              Responder
            </button>

            {hasReplies && (
              <button
                onClick={() => setShowReplies(!showReplies)}
                className="text-sm text-gray-500 hover:text-gray-700"
              >
                {showReplies ? 'Ocultar' : 'Ver'} {replies.length} {replies.length === 1 ? 'respuesta' : 'respuestas'}
              </button>
            )}
          </div>

          {/* Replies */}
          {showReplies && hasReplies && (
            <div className="mt-3 space-y-3">
              {replies.map(reply => (
                <CommentItem
                  key={reply.id}
                  comment={reply}
                  onReply={onReply}
                  onEdit={onEdit}
                  onDelete={onDelete}
                  level={level + 1}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CommentItem;
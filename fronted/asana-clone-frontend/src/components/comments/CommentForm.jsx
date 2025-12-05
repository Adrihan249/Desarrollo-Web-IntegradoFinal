// src/components/comments/CommentForm.jsx
import { useState } from 'react';
import { Send, AtSign } from 'lucide-react';
import Button from '../common/Button';
import Avatar from '../common/Avatar';
import { useAuth } from '../../hooks/useAuth';

const CommentForm = ({ taskId, parentCommentId = null, onSubmit, onCancel, placeholder = "Escribe un comentario..." }) => {
  const { user } = useAuth();
  const [content, setContent] = useState('');
  const [mentionedUsers, setMentionedUsers] = useState([]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;

    setIsSubmitting(true);
    try {
      await onSubmit({
        content: content.trim(),
        parentCommentId,
        mentionedUserIds: mentionedUsers.map(u => u.id)
      });
      setContent('');
      setMentionedUsers([]);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      handleSubmit(e);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3"><div className="flex gap-3">
        <Avatar user={user} size="sm" />
        <div className="flex-1">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder={placeholder}
            rows={parentCommentId ? 2 : 3}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
            disabled={isSubmitting}
          />
          <div className="flex items-center justify-between mt-2">
            <div className="flex items-center gap-2">
              <button
                type="button"
                className="text-sm text-gray-500 hover:text-gray-700 flex items-center gap-1"
              >
                <AtSign className="w-4 h-4" />
                Mencionar
              </button>
            </div>
            <div className="flex items-center gap-2">
              {parentCommentId && (
                <Button
                  type="button"
                  variant="secondary"
                  size="sm"
                  onClick={onCancel}
                  disabled={isSubmitting}
                >
                  Cancelar
                </Button>
              )}
              <Button
                type="submit"
                variant="primary"
                size="sm"
                disabled={!content.trim() || isSubmitting}
                isLoading={isSubmitting}
              >
                <Send className="w-4 h-4 mr-1" />
                {parentCommentId ? 'Responder' : 'Comentar'}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </form>
  );
};
export default CommentForm;
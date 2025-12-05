import { useState, useEffect, useMemo } from 'react';
import { 
  MessageSquare, 
  Search, 
  Plus, 
  Shield,
  X,
  Send,
  Smile,
  Paperclip,
  Edit2,
  Trash2,
  AlertCircle
} from 'lucide-react';

// URL base de la API
const API_URL = 'http://localhost:8080/api';

const ChatPage = () => {
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [searchEmail, setSearchEmail] = useState('');
  const [showNewChat, setShowNewChat] = useState(false);
  const [showBlockedUsers, setShowBlockedUsers] = useState(false);
  const [blockedUsers, setBlockedUsers] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [editingMessage, setEditingMessage] = useState(null);

  // Estado para manejar alertas y modales (reemplazo de alert/confirm)
  const [messageBox, setMessageBox] = useState({
    isVisible: false,
    content: '',
    onConfirm: null,
    isConfirm: false,
  });

  const currentUserId = useMemo(() => {
    try {
      // Intenta obtener el ID del usuario actual del localStorage
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      return user?.id || null;
    } catch (e) {
      console.error("Error al parsear el usuario del localStorage", e);
      return null;
    }
  }, []);

  const showAlert = (content) => {
    setMessageBox({ isVisible: true, content, onConfirm: null, isConfirm: false });
  };

  const showConfirm = (content, onConfirm) => {
    setMessageBox({ isVisible: true, content, onConfirm, isConfirm: true });
  };

  const closeMessageBox = () => {
    setMessageBox({ isVisible: false, content: '', onConfirm: null, isConfirm: false });
  };

  useEffect(() => {
    loadConversations();
    loadUnreadCount();
    loadBlockedUsers();
  }, []);

  useEffect(() => {
    if (selectedConversation && selectedConversation.otherUser.id) {
      loadMessages(selectedConversation.otherUser.id);
      markAsRead(selectedConversation.otherUser.id);
    }
  }, [selectedConversation]);

  const fetchAPI = async (url, options = {}) => {
    const token = localStorage.getItem('token');
    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers,
      },
    });

    if (!response.ok) {
      // Capturamos el error de la API para dar feedback específico
      const errorText = await response.text();
      console.error(`HTTP error! status: ${response.status}`, errorText);
      throw new Error(errorText || `Error del servidor: ${response.status}`);
    }

    return response.status === 204 ? null : response.json();
  };

  const loadConversations = async () => {
    try {
      const data = await fetchAPI(`${API_URL}/direct-messages/conversations`);
      setConversations(data);
    } catch (error) {
      console.error('Error loading conversations:', error);
      showAlert('Error al cargar conversaciones.');
    }
  };

  const loadMessages = async (otherUserId) => {
    try {
      setLoading(true);
      // Aseguramos que otrosUserId no es nulo o 0
      if (!otherUserId) return;
      
      const data = await fetchAPI(`${API_URL}/direct-messages/conversations/${otherUserId}`);
      // Invertir el orden de los mensajes para que el más reciente esté al final
      setMessages(data ? data.reverse() : []); 
    } catch (error) {
      console.error('Error loading messages:', error);
      showAlert('Error al cargar mensajes.');
    } finally {
      setLoading(false);
    }
  };

  const loadUnreadCount = async () => {
    try {
      const count = await fetchAPI(`${API_URL}/direct-messages/unread/count`);
      setUnreadCount(count);
    } catch (error) {
      console.error('Error loading unread count:', error);
    }
  };

  const loadBlockedUsers = async () => {
    try {
      const data = await fetchAPI(`${API_URL}/users/blocks`);
      setBlockedUsers(data);
    } catch (error) {
      console.error('Error loading blocked users:', error);
    }
  };

  const startNewChat = async () => {
    if (!searchEmail.trim()) return;

    try {
      const data = await fetchAPI(`${API_URL}/users/search?email=${searchEmail}`);
      if (data) {
        if (data.id === currentUserId) {
          showAlert('No puedes iniciar un chat contigo mismo.');
          return;
        }

        setSelectedConversation({
          otherUser: data,
          lastMessage: null,
          unreadCount: 0,
          sharedProjectNames: [],
          isProjectMember: false
        });
        setShowNewChat(false);
        setSearchEmail('');
      }
    } catch (error) {
      showAlert('Usuario no encontrado.');
    }
  };

  const sendMessage = async () => {
    if (!newMessage.trim() || !selectedConversation || !currentUserId) return;

    const receiverId = selectedConversation.otherUser.id;

    // *** FIX PARA EL ERROR 400 (Automensaje) ***
    if (receiverId === currentUserId) {
      showAlert('Error: No puedes enviarte mensajes a ti mismo.');
      return;
    }
    // *******************************************

    try {
      if (editingMessage) {
        await fetchAPI(`${API_URL}/direct-messages/${editingMessage.id}`, {
          method: 'PUT',
          body: JSON.stringify({ content: newMessage }),
        });
        setEditingMessage(null);
      } else {
        // Línea 47 (aproximadamente) del original
        await fetchAPI(`${API_URL}/direct-messages`, { 
          method: 'POST',
          body: JSON.stringify({
            receiverId: receiverId,
            content: newMessage,
            type: 'TEXT'
          }),
        });
      }

      setNewMessage('');
      loadMessages(receiverId);
      loadConversations();
    } catch (error) {
      showAlert(`Error al enviar mensaje: ${error.message}`);
    }
  };

  const handleDeleteMessage = (messageId) => {
    showConfirm('¿Estás seguro de que quieres eliminar este mensaje? Esta acción es irreversible.', async () => {
      try {
        await fetchAPI(`${API_URL}/direct-messages/${messageId}`, {
          method: 'DELETE',
        });
        loadMessages(selectedConversation.otherUser.id);
        closeMessageBox();
      } catch (error) {
        showAlert('Error al eliminar mensaje.');
      }
    });
  };

  const addReaction = async (messageId, emoji) => {
    try {
      await fetchAPI(`${API_URL}/direct-messages/${messageId}/reactions`, {
        method: 'POST',
        body: JSON.stringify({ emoji }),
      });
      loadMessages(selectedConversation.otherUser.id);
    } catch (error) {
      console.error('Error adding reaction:', error);
    }
  };

  const handleBlockUser = (userId) => {
    showConfirm('¿Bloquear a este usuario? No podrán enviarte más mensajes.', async () => {
      try {
        await fetchAPI(`${API_URL}/users/blocks`, {
          method: 'POST',
          body: JSON.stringify({ blockedUserId: userId }),
        });
        showAlert('Usuario bloqueado exitosamente.');
        setSelectedConversation(null);
        loadConversations();
        loadBlockedUsers();
        closeMessageBox();
      } catch (error) {
        showAlert('Error al bloquear usuario.');
      }
    });
  };

  const unblockUser = async (userId) => {
    try {
      await fetchAPI(`${API_URL}/users/blocks/${userId}`, {
        method: 'DELETE',
      });
      loadBlockedUsers();
      showAlert('Usuario desbloqueado.');
    } catch (error) {
      showAlert('Error al desbloquear usuario.');
    }
  };

  const markAsRead = async (otherUserId) => {
    try {
      await fetchAPI(`${API_URL}/direct-messages/conversations/${otherUserId}/read`, {
        method: 'PUT',
      });
      loadUnreadCount();
      loadConversations();
    } catch (error) {
      console.error('Error marking as read:', error);
    }
  };

  return (
    <div className="h-[calc(100vh-4rem)] flex bg-gray-50 font-inter">
      {/* Sidebar */}
      <div className="w-80 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-4 border-b border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h1 className="text-xl font-bold text-gray-900">Mensajes</h1>
            <div className="flex gap-2">
              <button
                onClick={() => setShowBlockedUsers(!showBlockedUsers)}
                className="p-2 hover:bg-gray-100 rounded-lg transition"
                title="Usuarios bloqueados"
              >
                <Shield className="w-5 h-5 text-gray-600" />
              </button>
              <button
                onClick={() => setShowNewChat(true)}
                className="p-2 bg-blue-500 hover:bg-blue-600 text-white rounded-lg transition shadow-md"
              >
                <Plus className="w-5 h-5" />
              </button>
            </div>
          </div>

          {unreadCount > 0 && (
            <div className="mb-3 px-3 py-2 bg-blue-50 text-blue-700 text-sm rounded-lg shadow-inner">
              {unreadCount} mensaje{unreadCount !== 1 ? 's' : ''} sin leer
            </div>
          )}
        </div>

        <div className="flex-1 overflow-y-auto">
          {conversations.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              <MessageSquare className="w-12 h-12 mx-auto mb-3 text-gray-400" />
              <p>No hay conversaciones</p>
              <p className="text-sm mt-2">Inicia un nuevo chat</p>
            </div>
          ) : (
            conversations.map((conv) => (
              <button
                key={conv.conversationId}
                onClick={() => setSelectedConversation(conv)}
                className={`w-full p-4 border-b border-gray-100 transition duration-150 ease-in-out text-left ${
                  selectedConversation?.conversationId === conv.conversationId ? 'bg-blue-100 border-blue-300' : 'hover:bg-gray-50'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-gray-900 truncate">
                      {conv.otherUser.fullName}
                    </p>
                    <p className="text-sm text-gray-600 truncate">
                      {conv.lastMessage?.content || 'Nueva conversación'}
                    </p>
                    {conv.isProjectMember && conv.sharedProjectNames?.length > 0 && (
                      <p className="text-xs text-blue-600 mt-1 font-medium">
                        Proyecto: {conv.sharedProjectNames[0]}
                      </p>
                    )}
                  </div>
                  {conv.unreadCount > 0 && (
                    <span className="ml-2 px-2 py-1 bg-red-500 text-white text-xs font-bold rounded-full animate-pulse">
                      {conv.unreadCount}
                    </span>
                  )}
                </div>
              </button>
            ))
          )}
        </div>
      </div>

      {/* Mensajes */}
      <div className="flex-1 flex flex-col">
        {selectedConversation ? (
          <>
            <div className="p-4 bg-white border-b border-gray-200 flex items-center justify-between shadow-sm">
              <div>
                <h2 className="font-semibold text-gray-900 text-lg">
                  {selectedConversation.otherUser.fullName}
                </h2>
                <p className="text-sm text-gray-600">
                  {selectedConversation.otherUser.email}
                </p>
                {!selectedConversation.isProjectMember && (
                  <div className="mt-2 flex items-center gap-1 text-xs text-amber-700 bg-amber-50 p-1.5 rounded-md">
                    <AlertCircle className="w-4 h-4" />
                    <span>Usuario desconocido - Puedes bloquear si no deseas responder.</span>
                  </div>
                )}
              </div>
              <button
                onClick={() => handleBlockUser(selectedConversation.otherUser.id)}
                className="px-3 py-1.5 bg-red-50 text-red-600 text-sm rounded-lg hover:bg-red-100 transition font-medium"
              >
                <Shield className="w-4 h-4 inline mr-1" />
                Bloquear
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-4 flex flex-col-reverse" style={{ direction: 'rtl' }}>
              <div style={{ direction: 'ltr' }}> {/* Contenedor de mensajes para invertir scroll */}
                {loading ? (
                  <div className="text-center text-gray-500 py-8">Cargando...</div>
                ) : messages.length === 0 ? (
                  <div className="text-center text-gray-500 mt-8">
                    <p>No hay mensajes aún</p>
                    <p className="text-sm mt-2">Envía el primer mensaje</p>
                  </div>
                ) : (
                  messages.map((msg) => {
                    const isOwn = msg.sender.id === currentUserId;
                    return (
                      <div key={msg.id} className={`flex mb-4 ${isOwn ? 'justify-end' : 'justify-start'}`}>
                        <div className={`max-w-md group relative ${isOwn ? 'bg-blue-600 text-white' : 'bg-white text-gray-800'} rounded-xl p-3 shadow-lg transition duration-200`}>
                          
                          {/* Contenido y botones */}
                          <div className="flex items-start justify-between gap-2">
                            <p className={msg.deleted ? 'italic opacity-70' : ''}>
                                {msg.deleted ? 'Este mensaje fue eliminado.' : msg.content}
                            </p>
                            
                            {/* Opciones (Editar/Eliminar) */}
                            {isOwn && !msg.deleted && (
                              <div className="flex gap-1 ml-2 text-xs">
                                <button
                                  onClick={() => {
                                    setEditingMessage(msg);
                                    setNewMessage(msg.content);
                                  }}
                                  className={`p-1 rounded transition hover:opacity-100 ${isOwn ? 'text-blue-200 hover:text-white' : 'text-gray-400 hover:text-gray-700'}`}
                                >
                                  <Edit2 className="w-3 h-3" />
                                </button>
                                <button
                                  onClick={() => handleDeleteMessage(msg.id)}
                                  className={`p-1 rounded transition hover:opacity-100 ${isOwn ? 'text-blue-200 hover:text-white' : 'text-gray-400 hover:text-gray-700'}`}
                                >
                                  <Trash2 className="w-3 h-3" />
                                </button>
                              </div>
                            )}
                          </div>
                          
                          {/* Footer del mensaje */}
                          <div className="flex items-center justify-between mt-1">
                            <p className={`text-xs ${isOwn ? 'text-blue-200' : 'text-gray-500'}`}>
                              {new Date(msg.createdAt).toLocaleTimeString('es-PE', {
                                hour: '2-digit',
                                minute: '2-digit'
                              })}
                              {msg.edited && ' (editado)'}
                              {msg.isRead && isOwn && ' ✓'} {/* Indicador de leído */}
                            </p>
                            
                            {/* Botón de reacción rápida */}
                            {!msg.deleted && (
                                <button
                                onClick={() => addReaction(msg.id, '👍')}
                                className="p-1 rounded transition hover:bg-black/20"
                                title="Reaccionar con 👍"
                                >
                                <Smile className={`w-4 h-4 ${isOwn ? 'text-blue-200' : 'text-gray-400'}`} />
                                </button>
                            )}
                          </div>
                          
                          {/* Contador de Reacciones */}
                          {msg.reactionCounts && Object.keys(msg.reactionCounts).length > 0 && (
                            <div className="absolute -bottom-2 -right-1 flex gap-1 bg-gray-200 dark:bg-gray-700 rounded-full py-0.5 px-1 shadow-md">
                              {Object.entries(msg.reactionCounts).map(([emoji, count]) => (
                                <span key={emoji} className="text-xs">
                                  {emoji} {count}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div className="p-4 bg-white border-t border-gray-200 shadow-inner">
              {editingMessage && (
                <div className="mb-3 px-3 py-2 bg-amber-50 text-amber-700 text-sm rounded-lg flex items-center justify-between border border-amber-200">
                  <span>Editando mensaje: **{editingMessage.content.substring(0, 30)}...**</span>
                  <button onClick={() => { setEditingMessage(null); setNewMessage(''); }} className="hover:text-amber-900 transition">
                    <X className="w-4 h-4" />
                  </button>
                </div>
              )}
              <div className="flex gap-2">
                <button
                    className="p-3 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition"
                    title="Adjuntar archivo (No implementado)"
                >
                    <Paperclip className="w-5 h-5" />
                </button>
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                  placeholder="Escribe un mensaje..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150"
                />
                <button
                  onClick={sendMessage}
                  disabled={!newMessage.trim()}
                  className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:opacity-50 transition shadow-md"
                >
                  <Send className="w-5 h-5" />
                </button>
              </div>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-500">
            <div className="text-center">
              <MessageSquare className="w-16 h-16 mx-auto mb-4 text-gray-400" />
              <p className="text-xl font-semibold">Mensajes Directos</p>
              <p className="text-sm mt-2">Selecciona una conversación para empezar a chatear.</p>
            </div>
          </div>
        )}
      </div>

      {/* Modales */}
      {showNewChat && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900">Iniciar Nuevo Chat</h2>
              <button onClick={() => setShowNewChat(false)} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-5 h-5 text-gray-600" />
              </button>
            </div>
            <p className="text-sm text-gray-600 mb-4">Busca un usuario por su dirección de correo electrónico.</p>
            <input
              type="email"
              value={searchEmail}
              onChange={(e) => setSearchEmail(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && startNewChat()}
              placeholder="Ingresa el email del usuario"
              className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 mb-4 transition"
            />
            <button
              onClick={startNewChat}
              disabled={!searchEmail.trim()}
              className="w-full px-4 py-3 bg-blue-500 text-white rounded-xl hover:bg-blue-600 disabled:opacity-50 transition shadow-md"
            >
              <Search className="w-5 h-5 inline mr-2" />
              Buscar e Iniciar Chat
            </button>
          </div>
        </div>
      )}

      {showBlockedUsers && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900">Usuarios Bloqueados</h2>
              <button onClick={() => setShowBlockedUsers(false)} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-5 h-5 text-gray-600" />
              </button>
            </div>
            {blockedUsers.length === 0 ? (
              <p className="text-gray-500 text-center py-8">No tienes usuarios bloqueados.</p>
            ) : (
              <div className="space-y-3 max-h-80 overflow-y-auto">
                {blockedUsers.map((user) => (
                  <div key={user.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
                    <div>
                      <p className="font-medium text-gray-900">{user.fullName}</p>
                      <p className="text-sm text-gray-600">{user.email}</p>
                    </div>
                    <button
                      onClick={() => unblockUser(user.id)}
                      className="px-3 py-1 bg-green-500 text-white text-sm rounded-lg hover:bg-green-600 transition shadow-sm"
                    >
                      Desbloquear
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Message Box (Alert/Confirm) */}
      {messageBox.isVisible && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[100]">
          <div className="bg-white rounded-xl p-6 w-full max-w-sm shadow-2xl text-center">
            <AlertCircle className="w-8 h-8 mx-auto mb-3 text-red-500" />
            <p className="text-lg font-semibold mb-4">{messageBox.isConfirm ? 'Confirmación' : 'Notificación'}</p>
            <p className="text-gray-700 mb-6">{messageBox.content}</p>
            <div className="flex justify-center gap-3">
              <button
                onClick={closeMessageBox}
                className={`px-4 py-2 rounded-lg font-medium transition ${messageBox.isConfirm ? 'bg-gray-200 hover:bg-gray-300 text-gray-800' : 'bg-blue-500 hover:bg-blue-600 text-white'}`}
              >
                {messageBox.isConfirm ? 'Cancelar' : 'Cerrar'}
              </button>
              {messageBox.isConfirm && (
                <button
                  onClick={messageBox.onConfirm}
                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 font-medium"
                >
                  Confirmar
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatPage;